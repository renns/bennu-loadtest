package com.qoid.bennu.loadtest.actors

import java.net.URI
import java.util.UUID

import akka.actor.Actor
import com.qoid.bennu.loadtest._
import com.qoid.bennu.loadtest.model._
import net.liftweb.json._

class SessionActor extends Actor {
  implicit val formats = DefaultFormats

  private val httpClient = context.actorSelection("/user/httpClient")

  private val sessionState = new SessionState

  override def receive: Receive = {

    case Login(authenticationId, password) =>
      handleLogin(authenticationId, password)

    case HttpResponse(
      statusCode,
      JObject(List(JField("channelId", JString(channelId)), JField("aliasIid", JString(aliasIid))))
    ) =>
      handleLoginResponse(channelId, aliasIid)

    case QueryResponse("query", _, tpe, JString(c), JArray(results), _, _, _) if c.startsWith("initial_data_load-") =>
      handleInitialDataLoad(tpe, results)

    case QueryResponse("squery", _, tpe, JString(c), JArray(result :: Nil), _, _, Some("insert")) if c.startsWith("initial_data_load-") =>
      handleDataInsertedOrUpdated(tpe, result)

    case QueryResponse("squery", _, tpe, JString(c), JArray(result :: Nil), _, _, Some("update")) if c.startsWith("initial_data_load-") =>
      handleDataInsertedOrUpdated(tpe, result)

    case QueryResponse("squery", _, tpe, JString(c), JArray(result :: Nil), _, _, Some("delete")) if c.startsWith("initial_data_load-") =>
      handleDataDeleted(tpe, result)

    case RunTask(task, metricId) =>
      handleRunTask(task, metricId)

    case ChannelResponse(true, JString(c), _, None) =>
      handleChannelResponse(c)

    case Logout =>
      handleLogout()

    case DeleteAgent =>
      handleDeleteAgent()

  }

  private def handleLogin(authenticationId: String, password: String): Unit = {
    val uri = new URI(s"${Config.serverUri}/api/channel/create/${authenticationId}?password=${password}")
    httpClient ! GetHttpRequest(uri)
  }

  private def handleLoginResponse(channelId: String, aliasIid: String): Unit = {
    sessionState.longPoller.foreach(longPoller => context.stop(longPoller))

    sessionState.clear()
    sessionState.channelId = Some(channelId)
    sessionState.aliasIid = Some(aliasIid)
    sessionState.longPoller = Some(context.actorOf(LongPollActor.props(channelId), s"longPoller-${channelId}"))

    sessionState.getLongPoller ! StartPolling

    httpClient ! ChannelRequestAssist.createLocalQueryRequest(channelId, List(
      ("initial_data_load-agent", QueryRequest("agent", "", None, true, Nil, true, true)),
      ("initial_data_load-alias", QueryRequest("alias", "", None, true, Nil, true, true)),
      ("initial_data_load-connection", QueryRequest("connection", "", None, true, Nil, true, true)),
      ("initial_data_load-label", QueryRequest("label", "", None, true, Nil, true, true)),
      ("initial_data_load-labelchild", QueryRequest("labelchild", "", None, true, Nil, true, true)),
      ("initial_data_load-profile", QueryRequest("profile", "", None, true, Nil, true, true))
    ))
  }

  private def handleInitialDataLoad(tpe: String, results: List[JValue]): Unit = {
    tpe match {
      case "agent" =>
        sessionState.agent = Some(results.head.extract[Agent])
        sessionState.agentLoaded = true
      case "alias" =>
        results.map(_.extract[Alias]).foreach(x => sessionState.aliases.put(x.iid, x))
        sessionState.aliasesLoaded = true
      case "connection" =>
        results.map(_.extract[Connection]).foreach(x => sessionState.connections.put(x.iid, x))
        sessionState.connectionsLoaded = true
      case "label" =>
        results.map(_.extract[Label]).foreach(x => sessionState.labels.put(x.iid, x))
        sessionState.labelsLoaded = true
      case "labelchild" =>
        results.map(_.extract[LabelChild]).foreach(x => sessionState.labelChilds.put(x.iid, x))
        sessionState.labelChildsLoaded = true
      case "profile" =>
        sessionState.profile = Some(results.head.extract[Profile])
        sessionState.profileLoaded = true
      case _ => throw new Exception("Unexpected initial data load type: " + tpe)
    }

    if (
      !sessionState.isLoaded &&
        sessionState.agentLoaded &&
        sessionState.aliasesLoaded &&
        sessionState.connectionsLoaded &&
        sessionState.labelsLoaded &&
        sessionState.labelChildsLoaded &&
        sessionState.profileLoaded
    ) {
      context.parent ! LoggedIn
      sessionState.isLoaded = true
    }
  }

  private def handleDataInsertedOrUpdated(tpe: String, result: JValue): Unit = {
    tpe match {
      case "agent" => sessionState.agent = Some(result.extract[Agent])
      case "alias" =>
        val alias = result.extract[Alias]
        sessionState.aliases.put(alias.iid, alias)
      case "connection" =>
        val connection = result.extract[Connection]
        sessionState.connections.put(connection.iid, connection)
      case "label" =>
        val label = result.extract[Label]
        sessionState.labels.put(label.iid, label)
      case "labelchild" =>
        val labelchild = result.extract[LabelChild]
        sessionState.labelChilds.put(labelchild.iid, labelchild)
      case "profile" => Some(result.extract[Profile])
      case _ => throw new Exception("Unexpected initial data load type: " + tpe)
    }
  }

  private def handleDataDeleted(tpe: String, result: JValue): Unit = {
    tpe match {
      case "agent" => sessionState.agent = None
      case "alias" => sessionState.aliases.remove(result.extract[Alias].iid)
      case "connection" => sessionState.connections.remove(result.extract[Connection].iid)
      case "label" => sessionState.labels.remove(result.extract[Label].iid)
      case "labelchild" => sessionState.labelChilds.remove(result.extract[LabelChild].iid)
      case "profile" => sessionState.profile = None
      case _ => throw new Exception("Unexpected initial data load type: " + tpe)
    }
  }

  private def handleRunTask(task: Task, metricId: UUID): Unit = {
    httpClient ! TaskAssist.createTaskRequest(sessionState, task, metricId.toString)
  }

  private def handleChannelResponse(c: String): Unit = {
    try {
      val metricId = UUID.fromString(c)
      context.parent ! TaskComplete(metricId)
    } catch {
      case e: Exception => //TODO: log exception
    }
  }

  private def handleLogout(): Unit = {
    sessionState.longPoller.foreach(_ ! StopPolling)
  }

  private def handleDeleteAgent(): Unit = {
    httpClient ! ChannelRequestAssist.createDeleteAgentRequest(sessionState.getChannelId, "delete_agent")
    handleLogout()
  }
}

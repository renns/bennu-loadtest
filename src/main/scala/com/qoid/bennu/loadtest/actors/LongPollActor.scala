package com.qoid.bennu.loadtest.actors

import java.net.URI

import akka.actor.Actor
import akka.actor.Props
import com.qoid.bennu.loadtest._
import com.qoid.bennu.loadtest.model.ChannelResponse
import com.qoid.bennu.loadtest.model.QueryResponse
import net.liftweb.json._

object LongPollActor {
  def props(channelId: String): Props = Props(new LongPollActor(channelId))
}

class LongPollActor(channelId: String) extends Actor {
  private val httpClient = context.actorSelection("/user/httpClient")
  private val uri = new URI(s"${Config.serverUri}/api/channel/poll/${channelId}/${Config.pollTimeout}")
  private var isPolling = false

  override def receive: Receive = {
    case StartPolling =>
      isPolling = true
      httpClient ! GetHttpRequest(uri)

    case StopPolling =>
      isPolling = false

    case HttpResponse(statusCode, body) =>
      implicit val formats = DefaultFormats

      body match {
        case JArray(messages) =>
          for (message <- messages) {
            message \ "handle" match {
              case JNothing =>
                val channelResponse = message.extract[ChannelResponse]
                context.parent ! channelResponse

              case _ =>
                val queryResponse = message.extract[QueryResponse]
                context.parent ! queryResponse
            }
          }

        case _ => //TODO: log warning
      }

      if (isPolling) httpClient ! GetHttpRequest(uri)
  }
}

package com.qoid.bennu.loadtest.actors

import java.net.URI

import akka.actor.Actor
import akka.actor.Props
import com.qoid.bennu.loadtest._

class AgentActor extends Actor {
  private val httpClient = context.actorSelection("/user/httpClient")

  override def receive: Receive = {
    case CreateAgent(name, password) =>
      val uri = new URI(s"${Config.serverUri}/api/agent/create/${name}?password=${password}")
      httpClient ! GetHttpRequest(uri)

    case DeleteAgent(name, password) =>
      val session = context.actorOf(Props[SessionActor], name)
      session ! Login(name, password)

    case LoggedIn =>
      sender() ! DeleteAgent
  }
}

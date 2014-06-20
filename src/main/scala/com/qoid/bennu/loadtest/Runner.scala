package com.qoid.bennu.loadtest

import java.util.UUID

import akka.actor.ActorSystem
import akka.actor.Props
import com.qoid.bennu.loadtest.actors._
import net.liftweb.json.JsonDSL._

object Runner extends App {
  val system = ActorSystem("bennuLoadTest")
  system.actorOf(Props[HttpClientActor], "httpClient")
  system.actorOf(Props[TimerActor], "timer")
  system.actorOf(Props[MetricsActor], "metrics")
  val agentActor = system.actorOf(Props[AgentActor], "agent")
  val testRunnerActor = system.actorOf(Props[TestRunnerActor], "testRunner")

  val amountOfSessions = 100
  val operationDelay = 2000

  println("Creating agents...")

  val agentList = createAgentList(amountOfSessions)

  agentList.foreach { a =>
    agentActor ! CreateAgent(a._1, a._2)
    Thread.sleep(2000)
  }

  val credentials = agentList.map(a => (a._1 + ".Anonymous", a._2))
  val tasks = List(InsertContent("TEXT", "text" -> "Content"), InsertLabel("Label"))

  println("Running test...")

  testRunnerActor ! StartTest(credentials, operationDelay, tasks)
  Thread.sleep(1 * 60 * 1000)

  testRunnerActor ! StopTest
  Thread.sleep(Math.max(operationDelay, 1000))

  println("Deleting agents...")

  agentList.foreach { a =>
    agentActor ! DeleteAgent(a._1, a._2)
    Thread.sleep(2000)
  }

  system.shutdown()
  system.awaitTermination()

  private def createAgentList(amount: Int): Iterable[(String, String)] = {
    (1 to amount).map(_ => (UUID.randomUUID().toString, "password"))
  }
}

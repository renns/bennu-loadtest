package com.qoid.bennu.loadtest.actors

import java.util.UUID

import akka.actor.{ActorRef, Actor, Props}
import com.qoid.bennu.loadtest._

import scala.collection.mutable

class TestRunnerActor extends Actor {
  private var isRunning = false
  private val sessionStates = mutable.HashMap.empty[String, Boolean]
  private var delay: Long = 0
  private var taskList: List[Task] = Nil
  private val metrics = context.actorSelection("/user/metrics")
  private val timer = context.actorSelection("/user/timer")

  override def receive: Receive = {
    case StartTest(credentials, operationDelay, tasks) =>
      if (isRunning) throw new Exception("Test already running")

      isRunning = true
      delay = operationDelay
      taskList = tasks
      context.children.foreach(context.stop)

      for ((authenticationId, password) <- credentials) {
        val session = context.actorOf(Props[SessionActor], s"session-${authenticationId}")
        sessionStates.put(session.path.name, false)
        session ! Login(authenticationId, password)
      }

    case StopTest=>
      isRunning = false
      delay = 0
      taskList = Nil
      sessionStates.clear()
      context.children.foreach(_ ! Logout)
      metrics ! PrintMetrics

    case LoggedIn =>
      sessionStates.put(sender().path.name, true)

      if (sessionStates.values.count(!_) == 0) {
        context.children.foreach(runTask)
      }

    case TaskComplete(metricId) =>
      metrics ! OperationMetricStop(metricId)
      timer ! StartTimer(delay, sender().path.name)

    case TimerElapsed(id) =>
      context.child(id).foreach(runTask)
  }

  private def runTask(session: ActorRef): Unit = {
    if (isRunning) {
      val metricId = UUID.randomUUID()
      metrics ! OperationMetricStart(metricId)
      session ! RunTask(getTask, metricId)
    }
  }

  private def getTask: Task = taskList((Math.random() * taskList.size).toInt)
}

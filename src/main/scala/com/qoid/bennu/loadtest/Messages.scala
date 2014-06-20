package com.qoid.bennu.loadtest

import java.net.URI
import java.util.UUID

import net.liftweb.json._

// HttpClientActor messages
case class GetHttpRequest(uri: URI)
case class PostHttpRequest(uri: URI, body: JValue, cookie: Option[(String, String)])
case class HttpResponse(statusCode: Int, body: JValue)

// TimerActor messages
case class StartTimer(milliseconds: Long, id: String)
case class TimerElapsed(id: String)

// MetricsActor messages
case class OperationMetricStart(id: UUID)
case class OperationMetricStop(id: UUID)
case object PrintMetrics

// AgentActor messages
case class CreateAgent(name: String, password: String)
case class DeleteAgent(name: String, password: String)
case object DeleteAgent

// TestRunnerActor messages
case class StartTest(credentials: Iterable[(String, String)], operationDelay: Long, tasks: List[Task])
case object StopTest
case class RunTask(task: Task, metricId: UUID)
case class TaskComplete(metricId: UUID)

// SessionActor messages
case class Login(authenticationId: String, password: String)
case object LoggedIn
case object Logout

// LongPollActor messages
case object StartPolling
case object StopPolling

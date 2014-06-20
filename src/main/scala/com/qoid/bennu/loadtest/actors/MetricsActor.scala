package com.qoid.bennu.loadtest.actors

import java.util.UUID

import akka.actor.Actor
import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer.Context
import com.qoid.bennu.loadtest.OperationMetricStart
import com.qoid.bennu.loadtest.OperationMetricStop
import com.qoid.bennu.loadtest.PrintMetrics

import scala.collection.mutable

class MetricsActor extends Actor {
  private val metrics = new MetricRegistry
  private val operationTimer = metrics.timer("Operations")
  private val reporter = ConsoleReporter.forRegistry(metrics).build()
  private val contexts = mutable.HashMap.empty[UUID, Context]

  override def receive: Receive = {
    case OperationMetricStart(id) => contexts.put(id, operationTimer.time())
    case OperationMetricStop(id) => contexts.remove(id).foreach(_.stop())
    case PrintMetrics => reporter.report()
  }
}

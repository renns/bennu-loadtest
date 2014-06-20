package com.qoid.bennu.loadtest.actors

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.actor.ActorRef
import com.qoid.bennu.loadtest.StartTimer
import com.qoid.bennu.loadtest.TimerElapsed
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask

class TimerActor extends Actor {
  private val timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS)

  override def receive: Receive = {
    case StartTimer(milliseconds, id) =>
      if (milliseconds > 0) {
        timer.newTimeout(createTimeTask(sender(), id), milliseconds, TimeUnit.MILLISECONDS)
      } else {
        sender() ! TimerElapsed(id)
      }
  }

  override def postStop(): Unit = {
    timer.stop()
  }

  private def createTimeTask(respondTo: ActorRef, id: String): TimerTask = {
    new TimerTask {
      override def run(t: Timeout): Unit = respondTo ! TimerElapsed(id)
    }
  }
}

package com.qoid.bennu.loadtest

import akka.actor.ActorRef
import com.qoid.bennu.loadtest.model._

import scala.collection.mutable

class SessionState() {
  var channelId: Option[String] = None
  var aliasIid: Option[String] = None

  var longPoller: Option[ActorRef] = None

  var agent: Option[Agent] = None
  val aliases = new mutable.HashMap[String, Alias]
  val connections = new mutable.HashMap[String, Connection]
  val labels = new mutable.HashMap[String, Label]
  val labelChilds = new mutable.HashMap[String, LabelChild]
  var profile: Option[Profile] = None

  var isLoaded = false
  var agentLoaded = false
  var aliasesLoaded = false
  var connectionsLoaded = false
  var labelsLoaded = false
  var labelChildsLoaded = false
  var profileLoaded = false

  def clear(): Unit = {
    channelId = None
    aliasIid = None

    longPoller = None

    agent = None
    aliases.clear()
    connections.clear()
    labels.clear()
    labelChilds.clear()
    profile = None

    isLoaded = false
    agentLoaded = false
    aliasesLoaded = false
    connectionsLoaded = false
    labelsLoaded = false
    labelChildsLoaded = false
    profileLoaded = false
  }

  def getChannelId: String = channelId.get
  def getAliasIid: String = aliasIid.get
  def getLongPoller: ActorRef = longPoller.get
}

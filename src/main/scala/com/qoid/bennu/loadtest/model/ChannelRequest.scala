package com.qoid.bennu.loadtest.model

import net.liftweb.json._

case class ChannelRequest(
  channel: String,
  requests: List[ChannelRequestRequest]
)

case class ChannelRequestRequest(
  path: String,
  context: JValue,
  parms: JValue
)

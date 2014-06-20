package com.qoid.bennu.loadtest.model

import net.liftweb.json._

case class ChannelResponse(
  success: Boolean,
  context: JValue,
  result: JValue,
  error: Option[ChannelResponseError] = None
)

case class ChannelResponseError(
  message: Option[String] = None,
  stacktrace: String
)

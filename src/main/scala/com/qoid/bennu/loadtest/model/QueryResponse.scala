package com.qoid.bennu.loadtest.model

import net.liftweb.json._

case class QueryResponse(
  responseType: String,
  handle: String,
  `type`: String,
  context: JValue,
  results: JValue,
  aliasIid: Option[String] = None,
  connectionIid: Option[String] = None,
  action: Option[String] = None
)

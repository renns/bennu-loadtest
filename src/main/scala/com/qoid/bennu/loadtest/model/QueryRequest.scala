package com.qoid.bennu.loadtest.model

case class QueryRequest(
  `type`: String,
  q: String,
  aliasIid: Option[String],
  local: Boolean,
  connectionIids: List[List[String]],
  historical: Boolean,
  standing: Boolean
)

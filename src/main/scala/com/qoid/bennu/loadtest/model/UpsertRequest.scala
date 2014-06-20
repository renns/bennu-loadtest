package com.qoid.bennu.loadtest.model

import net.liftweb.json._

case class UpsertRequest(
  `type`: String,
  instance: JValue,
  parentIid: Option[String],
  profileName: Option[String],
  profileImgSrc: Option[String],
  labelIids: List[String]
)

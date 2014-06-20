package com.qoid.bennu.loadtest

import net.liftweb.json._

sealed trait Task

case class InsertContent(contentType: String, data: JValue) extends Task
case class InsertLabel(name: String) extends Task

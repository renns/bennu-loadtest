package com.qoid.bennu.loadtest

import java.net.URI

import com.qoid.bennu.loadtest.model._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object ChannelRequestAssist {
  implicit val formats = DefaultFormats

  private val submitChannelUri = new URI(s"${Config.serverUri}/api/channel/submit")

  def createChannelRequest(channelId: String, requests: List[ChannelRequestRequest]): PostHttpRequest = {
    val body = Extraction.decompose(ChannelRequest(channelId, requests))

    PostHttpRequest(submitChannelUri, body, Some(("channel", channelId)))
  }

  def createUpsertRequest(
    channelId: String,
    context: String,
    tpe: String,
    instance: JValue,
    parentIid: Option[String] = None,
    profileName: Option[String] = None,
    profileImgSrc: Option[String] = None,
    labelIids: List[String] = Nil
  ): PostHttpRequest = {
    val upsertRequest = UpsertRequest(tpe, instance, parentIid, profileName, profileImgSrc, labelIids)

    createChannelRequest(
      channelId,
      List(ChannelRequestRequest("/api/upsert", context, Extraction.decompose(upsertRequest)))
    )
  }

  def createLocalQueryRequest(channelId: String, queries: List[(String, QueryRequest)]): PostHttpRequest = {
    createChannelRequest(channelId, queries.map(x => ChannelRequestRequest("/api/query", x._1, Extraction.decompose(x._2))))
  }

  def createDeleteAgentRequest(channelId: String, context: String): PostHttpRequest = {
    val deleteAgentRequest = DeleteAgentRequest(false)

    createChannelRequest(
      channelId,
      List(ChannelRequestRequest("/api/agent/delete", context, Extraction.decompose(deleteAgentRequest)))
    )
  }

  def createInsertContentRequest(
    channelId: String,
    context: String,
    aliasIid: String,
    contentType: String,
    data: JValue,
    labelIids: List[String]
  ): PostHttpRequest = {

    createUpsertRequest(
      channelId,
      context,
      "content",
      ("aliasIid" -> aliasIid) ~ ("contentType" -> contentType) ~ ("data" -> "data"),
      labelIids = labelIids
    )
  }

  def createInsertLabelRequest(channelId: String, context: String, name: String, parentIid: String): PostHttpRequest = {
    createUpsertRequest(
      channelId,
      context,
      "label",
      ("name" -> name) ~ ("data" -> ("color" -> "#7F7F7F")),
      parentIid = Some(parentIid)
    )
  }
}

package com.qoid.bennu.loadtest

object TaskAssist {
  def createTaskRequest(sessionState: SessionState, task: Task, context: String): PostHttpRequest = {
    task match {
      case InsertContent(contentType, data) =>
        val labelValues = sessionState.labels.values.toList
        val label = labelValues((Math.random() * labelValues.size).toInt)

        ChannelRequestAssist.createInsertContentRequest(
          sessionState.getChannelId,
          context,
          sessionState.getAliasIid,
          contentType,
          data,
          List(label.iid)
        )

      case InsertLabel(name) =>
        val labelValues = sessionState.labels.values.toList
        val label = labelValues((Math.random() * labelValues.size).toInt)

        ChannelRequestAssist.createInsertLabelRequest(
          sessionState.getChannelId,
          context,
          name,
          label.iid
        )
    }
  }
}

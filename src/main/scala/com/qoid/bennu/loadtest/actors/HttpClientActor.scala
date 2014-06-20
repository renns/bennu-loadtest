package com.qoid.bennu.loadtest.actors

import akka.actor.Actor
import akka.actor.ActorRef
import com.qoid.bennu.loadtest.GetHttpRequest
import com.qoid.bennu.loadtest.HttpResponse
import com.qoid.bennu.loadtest.PostHttpRequest
import net.liftweb.json._
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpResponse => HttpClientResponse}

class HttpClientActor extends Actor {
  private val httpClient = createHttpClient()

  override def receive: Receive = {
    case GetHttpRequest(uri) =>
      //println("GET -- " + uri.toString)
      executeHttpRequest(new HttpGet(uri.toString), sender())

    case PostHttpRequest(uri, body, cookie) =>
      val httpPost = new HttpPost(uri.toString)

      cookie.foreach { case (name, value) => httpPost.setHeader("Cookie", s"$name=$value") }
      httpPost.setHeader("Content-Type", "application/json")
      httpPost.setEntity(new StringEntity(pretty(render(body))))

      //println("POST -- " + uri.toString + "\n" + pretty(render(body)))
      executeHttpRequest(httpPost, sender())
  }

  override def preStart(): Unit = {
    httpClient.start()
  }

  override def postStop(): Unit = {
    httpClient.close()
  }

  private def createHttpClient(): CloseableHttpAsyncClient = {
    val clientBuilder = HttpAsyncClients.custom()
    clientBuilder.setMaxConnPerRoute(1000)
    clientBuilder.setMaxConnTotal(1000)
    clientBuilder.build()
  }

  private def executeHttpRequest(request: HttpUriRequest, respondTo: ActorRef): Unit = {
    httpClient.execute(request, new FutureCallback[HttpClientResponse] {

      override def completed(response: HttpClientResponse): Unit = {
        val statusCode = response.getStatusLine.getStatusCode

        val body: JValue =
          if (statusCode == 200 && response.getEntity != null) parse(EntityUtils.toString(response.getEntity))
          else JNothing

        if (statusCode != 200) println("RESPONSE -- " + statusCode)
        //println("RESPONSE -- " + statusCode + "\n" + pretty(render(body)))

        respondTo ! HttpResponse(statusCode, body)
      }

      override def failed(e: Exception): Unit = throw e
      override def cancelled(): Unit = ()

    })
  }
}

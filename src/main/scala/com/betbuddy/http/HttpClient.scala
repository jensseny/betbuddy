package com.betbuddy.http

import grizzled.slf4j.Logging
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.{HttpResponse, HttpResponseCode}
import java.net.URL

sealed trait ResponseType {
  val statusCode: Int
  val response: String
}
case class Success(statusCode: Int, response: String) extends ResponseType
case class Failure(statusCode: Int, response: String) extends ResponseType
case class Redirect(statusCode: Int, response: String) extends ResponseType

class HttpClient extends Logging {

  implicit val client: ApacheHttpClient = new ApacheHttpClient
  private val headers = List(("User-Agent", """Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0"""),
                             ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))

  def get(url: String): ResponseType = {
    logger.info("Attempting to execute: %s" format url)
    logger.info("  method: GET")
    val request = GET(new URL(url)).addHeaders(headers)
    val response = request.executeUnsafe
    parseResponse(response)
  }

  def post(url: String, body: String): ResponseType = {
    logger.info("Attempting to execute: %s" format url)
    logger.info("  method: POST with %s" format body)
    val request = POST(new URL(url)).addBody(body).addHeaders(("Content-Type", "application/x-www-form-urlencoded") :: headers)
    val response = request.executeUnsafe
    parseResponse(response)
  }

  def parseResponse(response: HttpResponse): ResponseType = {
    response.code match {
      case HttpResponseCode.Ok => Success(response.code, response.bodyString)
      case HttpResponseCode.TemporaryRedirect => Redirect(response.code, response.headers.get.list.filter(_._1.equals("Location")).head._2)
      case HttpResponseCode.NotFound => Failure(response.code, response.bodyString)
      case _ => Failure(response.code, "Whoops, not handled yet! " + response.bodyString)
    }
  }
}

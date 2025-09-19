/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.{EqualToJsonPattern, EqualToPattern}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.Json

object WireMockConstants {
  val stubPort = 11111
  val stubHost = "localhost"
}

trait WireMockHelper extends BeforeAndAfterAll with BeforeAndAfterEach with AuthStubs {
  this: Suite =>

  val wireMockHost: String                = WireMockConstants.stubHost
  val wireMockPort: Int                   = WireMockConstants.stubPort
  val mockServerUrl                       = s"http://$wireMockHost:$wireMockPort"
  protected val endpointConfigurationPath = "microservice.services"

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
    WireMock.configureFor("localhost", wireMockPort)

  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    server.resetAll()
  }

  override def afterAll(): Unit = {
    server.stop()
    super.afterAll()
  }

  def stubAuthorised(appaId: String): Unit =
    stubPost(authUrl, OK, authRequest, authOKResponse(appaId))

  def verifyAuthorised(): Unit =
    verifyPost(authUrl)

  def stubPostResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    server.stubFor(
      post(urlPathMatching(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  def stubGetResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    server.stubFor(
      WireMock
        .get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  protected def getWireMockAppConfig(endpointNames: Seq[String]): Map[String, Any] =
    endpointNames
      .flatMap(
        endpointName =>
          Seq(
            s"$endpointConfigurationPath.$endpointName.host" -> wireMockHost,
            s"$endpointConfigurationPath.$endpointName.port" -> wireMockPort
          )
      )
      .toMap

  protected def getWireMockAppConfigWithRetry(endpointNames: Seq[String]): Map[String, Any] =
    endpointNames
      .flatMap(
        endpointName =>
          Seq(
            s"$endpointConfigurationPath.$endpointName.host"   -> wireMockHost,
            s"$endpointConfigurationPath.$endpointName.port"   -> wireMockPort,
            s"$endpointConfigurationPath.retry.retry-attempts" -> 1
          )
      )
      .toMap

  private def stripToPath(url: String) =
    if (url.startsWith("http://") || url.startsWith("https://"))
      url.dropWhile(_ != '/').dropWhile(_ == '/').dropWhile(_ != '/')
    else
      url

  private def urlWithParameters(url: String, parameters: Seq[(String, String)]) = {
    val queryParams = parameters
      .map {
        case (k, v) => s"$k=$v"
      }
      .mkString("&")

    s"${stripToPath(url)}?$queryParams"
  }

  def stubGet(url: String, status: Int, body: String): Unit =
    server.stubFor(
      WireMock.get(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(status).withBody(body))
    )

  def stubPostUnauthorised(
    url: String
  ): Unit =
    server.stubFor(
      WireMock.post(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(UNAUTHORIZED))
    )

  def stubPost(url: String, status: Int, requestBody: String, returnBody: String): Unit =
    server.stubFor(
      WireMock
        .post(urlEqualTo(stripToPath(url)))
        .withRequestBody(new EqualToJsonPattern(requestBody, true, false))
        .willReturn(aResponse().withStatus(status).withBody(returnBody))
    )

  def stubPut(url: String, status: Int, requestBody: String, returnBody: String): Unit =
    server.stubFor(
      WireMock
        .put(urlEqualTo(stripToPath(url)))
        .withRequestBody(new EqualToJsonPattern(requestBody, true, false))
        .willReturn(aResponse().withStatus(status).withBody(returnBody))
    )

  def verifyGet(url: String): Unit =
    server.verify(getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyGetWithParameters(url: String, parameters: Seq[(String, String)]): Unit =
    server.verify(getRequestedFor(urlEqualTo(urlWithParameters(url, parameters))))

  def verifyGetWithParametersAndHeaders(
    url: String,
    parameters: Seq[(String, String)] = Seq.empty,
    headers: Seq[(String, String)] = Seq.empty
  ): Unit = {
    val requestPattern = getRequestedFor(urlEqualTo(urlWithParameters(url, parameters)))
    val requestPatternWithHeaders = headers.foldLeft(requestPattern) {
      (pattern, header) =>
        pattern.withHeader(header._1, new EqualToPattern(header._2))
    }
    server.verify(requestPatternWithHeaders)
  }

  def verifyGetWithoutRetry(url: String): Unit =
    server.verify(1, getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyGetWithRetry(url: String): Unit =
    server.verify(2, getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPost(url: String): Unit =
    server.verify(postRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPut(url: String): Unit =
    server.verify(putRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPutWithoutRetry(url: String): Unit =
    server.verify(1, putRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyPutWithRetry(url: String): Unit =
    server.verify(2, putRequestedFor(urlEqualTo(stripToPath(url))))

}

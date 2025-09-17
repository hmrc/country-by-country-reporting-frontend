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

package controllers

import connectors.Connector
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSRequest}
import play.api.mvc.{Session, SessionCookieBaker}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.MongoSupport
import utils.AuthStubs

class IndexControllerISpec extends Connector with MongoSupport with AuthStubs {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("mongodb.uri" -> mongoUri)
      .build()

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override def beforeAll(): Unit = {
    dropDatabase()
    super.beforeAll()
  }

  "GET / IndexController.onPageLoad" - {
    "return OK when the user is authorised" in {
      lazy val baseUrl  = s"http://$wireMockHost:$wireMockPort"
      val downstreamUrl = s"$baseUrl/send-a-country-by-country-report/"
      def buildClient(): WSRequest =
        app.injector.instanceOf[WSClient].url(downstreamUrl)
      stubAuthorised("cbc12345")

      val session            = Session(Map("authToken" -> "abc123", "role" -> "admin"))
      val sessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
      val sessionCookie      = sessionCookieBaker.encodeAsCookie(session)
      val wsSessionCookie    = DefaultWSCookie(sessionCookie.name, sessionCookie.value)

      stubGet(
        downstreamUrl,
        OK,
        Json.toJson("auth successful?").toString
      )

      val response = await(
        buildClient()
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      response.body must not include "Authority Wizard"

    }
  }

}

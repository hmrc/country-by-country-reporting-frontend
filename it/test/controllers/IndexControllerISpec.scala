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
import play.api.libs.ws.WSClient
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

      stubAuthorised("cbc12345")

      stubGet(
        downstreamUrl,
        OK,
        Json.toJson("auth successful?").toString
      )

      val response = await(wsClient.url(s"http://localhost:10024/send-a-country-by-country-report/").get())

      response.status mustBe OK
//      verifyAuthorised()
      response.body must not include "Authority Wizard"

    }
  }

}

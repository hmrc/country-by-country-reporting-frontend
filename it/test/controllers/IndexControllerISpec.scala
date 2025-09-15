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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.MongoSupport
import utils.{ISpecBase, SpecCommonHelper}

class IndexControllerISpec
    extends PlaySpec
    with Matchers
    with ScalaFutures
    with GuiceOneServerPerSuite
    with ISpecBase
      with SpecCommonHelper
    with BeforeAndAfterEach
    with MongoSupport {

  private val url = s"http://localhost:$port/"

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> mongoUri
      )
      .build()

  lazy val repository: SessionRepository =
    app.injector.instanceOf[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    repository.collection.drop().toFuture().futureValue
  }

  "GET / (IndexController.onPageLoad)" must {

    "return OK when the user is authorised" in {
      authorised()
      lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

      val response = await(wsClient.url(url).get())

      response.status mustBe OK
      response.body must include("CBC")
    }

//    "redirect to unauthorised page when the user is not authorised" in {
//      unauthorised()
//
//      val response = await(wsClient.url(url).get())
//
//      response.status mustBe SEE_OTHER
//      response.header("Location").value must include("/unauthorised")
//    }
//
//    "redirect to login when there is no active session" in {
//      unauthorised()
//
//      val response = await(wsClient.url(url).get())
//
//      response.status mustBe SEE_OTHER
//      response.header("Location").value must include("/bas-gateway/sign-in")
//    }
  }
}

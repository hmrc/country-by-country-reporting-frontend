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

import models.UserAnswers
import play.api.http.Status._
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import play.api.mvc.{Session, SessionCookieBaker}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import utils.{ISpecBase, SpecCommonHelper}

class IndexControllerISpec
extends SpecCommonHelper
with ISpecBase
    with DefaultPlayMongoRepositorySupport[UserAnswers] {

  lazy val repository: SessionRepository = app.injector.instanceOf[SessionRepository]

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val signer = app.injector.instanceOf[CookieSigner]


  override def beforeEach(): Unit = {
    super.beforeEach()
    repository.collection.drop().toFuture().futureValue
  }
  "GET / (IndexController.onPageLoad)" must {

    "return OK when the user is authorised" in {

      authorised()

      val session = Session(Map("authToken" -> "abc123", "role" -> "admin"))
      val sessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
      val sessionCookie = sessionCookieBaker.encodeAsCookie(session)
      val wsSessionCookie = DefaultWSCookie(sessionCookie.name,sessionCookie.value)


      val response = await(buildClient()
        .addCookies(wsSessionCookie)
        .get())

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

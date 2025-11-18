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

import models.UserAnswers
import org.scalatestplus.play.PlaySpec
import pages.AgentClientIdPage
import play.api.http.Status.*
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

trait ISpecBehaviours extends PlaySpec with ISpecBase {

  export play.api.libs.ws.DefaultBodyWritables.*
  export play.api.libs.ws.DefaultBodyReadables.readableAsString

  lazy val wsClient: WSClient                = app.injector.instanceOf[WSClient]
  val session: Session                       = Session(Map("authToken" -> "abc123"))
  val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val sessionCookie: Cookie                  = sessionCookieBaker.encodeAsCookie(session)
  val wsSessionCookie: DefaultWSCookie       = DefaultWSCookie(sessionCookie.name, sessionCookie.value)
  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages       = MessagesImpl(Lang.defaultLang, messagesApi)

  def pageRedirectsWhenNotAuthorised(pageUrl: Option[String]): Unit = {

    "redirect to login when there is no active session" in {
      val response = await(
        buildClient(pageUrl)
          .withFollowRedirects(false)
          .get()
      )
      response.status mustBe SEE_OTHER
      response.header("Location").value must include("gg-sign-in")
    }
    "redirect to /unauthorised" in {
      stubUnauthorised("/auth/authorise")
      val response = await(
        buildClient(pageUrl)
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe SEE_OTHER
      response.header("Location") mustBe Some("/send-a-country-by-country-report/problem/unauthorised")
      verifyPost(authUrl)
    }

    "redirect to /problem/client-access when agent does not belong to access-group of the client" in {
      val userAnswers = emptyUserAnswers
        .set(AgentClientIdPage, "testClientId")
        .success
        .value

      repository.set(userAnswers)

      stubAuthorisedAgent()

      stubClientAccessProblem()

      val response = await(
        buildClient(pageUrl)
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe SEE_OTHER
      response.header("Location") mustBe Some("/send-a-country-by-country-report/agent/problem/client-access")
    }
  }

  def pageLoads(pageUrl: Option[String], pageTitle: String = "", userAnswers: UserAnswers = emptyUserAnswers): Unit =
    "load relative page" in {
      stubAuthorised("cbcId")

      await(repository.set(userAnswers))

      val response = await(
        buildClient(pageUrl)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      response.body must include(messages(pageTitle))

    }

  def pageSubmits(pageUrl: Option[String],
                  redirectLocation: String,
                  ua: UserAnswers = UserAnswers("internalId"),
                  requestBody: Map[String, Seq[String]] = Map("value" -> Seq("testValue"))
  ): Unit =
    "should submit form" in {
      stubAuthorised("testId")

      await(repository.set(ua))

      val response = await(
        buildClient(pageUrl)
          .addCookies(wsSessionCookie)
          .addHttpHeaders("Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(requestBody)
      )

      response.status mustBe SEE_OTHER
      response.header("Location").value must
        include(redirectLocation)
      verifyPost(authUrl)
    }

  def standardOnSubmit(pageUrl: Option[String], requestBody: Map[String, Seq[String]]): Unit = {
    "redirect to /individual-sign-in-problem for POST" in {

      stubAuthorisedIndividual("cbc12345")

      val response = await(
        buildClient(pageUrl)
          .addHttpHeaders("Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .post(requestBody)
      )

      response.status mustBe SEE_OTHER
      response.header("Location").value must include("/send-a-country-by-country-report/problem/individual-sign-in-problem")
    }

    "redirect to login when there is no active session for POST" in {
      val response = await(
        buildClient(pageUrl)
          .addHttpHeaders("Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(requestBody)
      )

      response.status mustBe SEE_OTHER
      response.header("Location").value must include("gg-sign-in")
    }

  }

}

/*
 * Copyright 2026 HM Revenue & Customs
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

package handlers

import base.SpecBase
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.{PageNotFoundView, ThereIsAProblemView}

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val messagesApi         = mock[MessagesApi]
  private val thereIsAProblemView = mock[ThereIsAProblemView]
  private val pageNotFoundView    = mock[PageNotFoundView]

  private val handler = new ErrorHandler(messagesApi, thereIsAProblemView, pageNotFoundView)

  private val fakeRequest = FakeRequest("GET", "/test")

  "ErrorHandler" - {

    "notFoundTemplate" - {
      "must return the rendered PageNotFoundView" in {
        when(pageNotFoundView.apply()(any(), any())).thenReturn(Html("<h1>Not found</h1>"))

        val result = handler.notFoundTemplate(fakeRequest).futureValue

        result.body must include("Not found")
      }
    }

    "standardErrorTemplate" - {
      "must return the rendered ThereIsAProblemView regardless of arguments" in {
        when(thereIsAProblemView.apply()(any(), any())).thenReturn(Html("<h1>There is a problem</h1>"))

        val result = handler
          .standardErrorTemplate("Title", "Heading", "Message")(fakeRequest)
          .futureValue

        result.body must include("There is a problem")
      }
    }

    "onClientError" - {
      "must return 404 with notFoundView when status is NOT_FOUND" in {
        when(pageNotFoundView.apply()(any(), any())).thenReturn(Html("<h1>Not found</h1>"))

        val result = handler
          .onClientError(fakeRequest, play.mvc.Http.Status.NOT_FOUND, "Not Found")
          .futureValue

        result.header.status mustBe play.api.http.Status.NOT_FOUND
      }

      "must redirect to ThereIsAProblemController for any other client error status" in {
        val result = handler
          .onClientError(fakeRequest, play.mvc.Http.Status.FORBIDDEN, "Forbidden")
          .futureValue

        result.header.status mustBe play.api.http.Status.SEE_OTHER
        result.header.headers.get("Location") mustBe Some(
          routes.ThereIsAProblemController.onPageLoad().url
        )
      }

      "must redirect to ThereIsAProblemController for a BAD_REQUEST status" in {
        val result = handler
          .onClientError(fakeRequest, play.mvc.Http.Status.BAD_REQUEST, "Bad Request")
          .futureValue

        result.header.status mustBe play.api.http.Status.SEE_OTHER
      }
    }
  }
}

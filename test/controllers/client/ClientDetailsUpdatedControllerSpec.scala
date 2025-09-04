/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.client

import base.SpecBase
import pages.{IsMigratedUserContactUpdatedPage, JourneyInProgressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.client.ClientDetailsUpdatedView

class ClientDetailsUpdatedControllerSpec extends SpecBase {

  "ClientDetailsUpdated Controller" - {

    "must return OK and the correct view for a GET" in {

      val ua          = emptyUserAnswers
      val userAnswers = ua.set(JourneyInProgressPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ClientDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
        verify(mockSessionRepository, times(1)).set(ua)
      }
    }

    "must set IsMigratedUserContactUpdatedPage as true" in {

      val ua          = emptyUserAnswers
      val userAnswers = ua.set(IsMigratedUserContactUpdatedPage, false).success.value
      val expectedUA  = ua.set(IsMigratedUserContactUpdatedPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ClientDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
        verify(mockSessionRepository, times(1)).set(expectedUA)
      }
    }
  }
}

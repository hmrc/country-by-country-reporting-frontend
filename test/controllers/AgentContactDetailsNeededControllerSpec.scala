/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import pages.ContactNamePage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.AgentContactDetailsNeededView

import scala.concurrent.Future

class AgentContactDetailsNeededControllerSpec extends SpecBase {

  "AgentContactDetailsNeeded Controller" - {

    "must return OK and the correct view for a GET when client details exist" in {

      val mockSubscriptionService = mock[SubscriptionService]
      val userAnswers             = emptyUserAnswers.set(ContactNamePage, "name").success.value

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      when(mockSubscriptionService.getContactDetails(any[UserAnswers])(any[HeaderCarrier])).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsNeededController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsNeededView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(hasClientContactDetails = true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when client details do not exist" in {

      val mockSubscriptionService = mock[SubscriptionService]

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      when(mockSubscriptionService.getContactDetails(any[UserAnswers])(any[HeaderCarrier])).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsNeededController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsNeededView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(hasClientContactDetails = false)(request, messages(application)).toString
      }
    }

    "must redirect to there is a problem if getContactDetails returns none " in {

      val mockSubscriptionService = mock[SubscriptionService]

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      when(mockSubscriptionService.getContactDetails(any[UserAnswers])(any[HeaderCarrier])).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsNeededController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.ThereIsAProblemController.onPageLoad().url)
      }
    }
  }
}



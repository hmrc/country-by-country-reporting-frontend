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

package controllers.agent

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import pages.{AgentClientIdPage, ContactNamePage, IsMigratedAgentContactUpdatedPage, JourneyInProgressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.agent.AgentContactDetailsUpdatedView

import scala.concurrent.Future

class AgentContactDetailsUpdatedControllerSpec extends SpecBase {

  "AgentContactDetailsUpdated Controller" - {

    "must return OK and the correct view for a GET when client contact details does not exist" in {

      val ua = emptyUserAnswers
        .set(ContactNamePage, "name")
        .success
        .value

      val userAnswers = ua.set(JourneyInProgressPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientSelected = false)(request, messages(application)).toString
        verify(mockSessionRepository, times(1)).set(ua)
      }
    }

    "must return OK and the correct view for a GET when client is selected by agent" in {

      val ua          = emptyUserAnswers.set(AgentClientIdPage, "clientID").success.value
      val userAnswers = ua.set(JourneyInProgressPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientSelected = true)(request, messages(application)).toString
        verify(mockSessionRepository, times(1)).set(ua)
      }
    }

    "must set IsMigratedAgentContactUpdatedPage as true" in {

      val ua          = emptyUserAnswers.set(AgentClientIdPage, "clientID").success.value
      val userAnswers = ua.set(IsMigratedAgentContactUpdatedPage, false).success.value
      val expectedUA  = ua.set(IsMigratedAgentContactUpdatedPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsUpdatedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsUpdatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientSelected = true)(request, messages(application)).toString
        verify(mockSessionRepository, times(1)).set(expectedUA)
      }
    }

  }
}

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
import forms.AgentFirstContactNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{AgentContactDetailsNavigator, FakeAgentContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.AgentFirstContactNamePage
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.agent.AgentFirstContactNameView

import scala.concurrent.Future

class AgentFirstContactNameControllerSpec extends SpecBase {
  val formProvider                       = new AgentFirstContactNameFormProvider()
  val form                               = formProvider()
  lazy val firstContactNameRoute: String = routes.AgentFirstContactNameController.onPageLoad(NormalMode).url
  "AgentFirstContactNameController" - {

    "Must return OK and correct view for GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, firstContactNameRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentFirstContactNameView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString()
      }
    }

    "Must populate view correctly on a GET, when the questions has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(AgentFirstContactNamePage, "answer").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, firstContactNameRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentFirstContactNameView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString()
      }
    }

    "Must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AgentContactDetailsNavigator].toInstance(new FakeAgentContactDetailsNavigator(onwardRoute)),
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, firstContactNameRoute).withFormUrlEncodedBody(("value", "answer"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "Must return a bad request, and errors, when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request   = FakeRequest(POST, firstContactNameRoute).withFormUrlEncodedBody(("value", ""))
        val result    = route(application, request).value
        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[AgentFirstContactNameView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString()
      }
    }
  }
}

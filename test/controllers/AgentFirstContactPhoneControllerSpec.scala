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
import forms.AgentFirstContactPhoneFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{ContactDetailsNavigator, FakeContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentFirstContactPhonePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.AgentFirstContactPhoneView

import scala.concurrent.Future

class AgentFirstContactPhoneControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AgentFirstContactPhoneFormProvider()
  val form         = formProvider()

  lazy val agentFirstContactPhoneRoute = routes.AgentFirstContactPhoneController.onPageLoad(NormalMode).url

  "AgentContactTelephoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentFirstContactPhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentFirstContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, "", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AgentFirstContactPhonePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentFirstContactPhoneRoute)

        val view = application.injector.instanceOf[AgentFirstContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), "", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ContactDetailsNavigator].toInstance(new FakeContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agentFirstContactPhoneRoute)
            .withFormUrlEncodedBody(("value", "0928273"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentFirstContactPhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentFirstContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "", NormalMode)(request, messages(application)).toString
      }
    }
  }
}

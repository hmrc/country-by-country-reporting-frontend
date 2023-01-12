/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.AgentFirstContactEmailFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{AgentContactDetailsNavigator, FakeAgentContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentFirstContactEmailPage, AgentFirstContactNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.agent.AgentFirstContactEmailView

import scala.concurrent.Future

class AgentFirstContactEmailControllerSpec extends SpecBase with MockitoSugar {
  val formProvider      = new AgentFirstContactEmailFormProvider()
  val form              = formProvider()
  val contactName       = "first contact name"
  val contactNamePlural = "first contact name’s"

  lazy val agentFirstContactEmailRoute: String = routes.AgentFirstContactEmailController.onPageLoad(NormalMode).url

  "AgentFirstContactEmail Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(AgentFirstContactNamePage, contactName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentFirstContactEmailRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentFirstContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AgentFirstContactEmailPage, "answer")
        .success
        .value
        .set(AgentFirstContactNamePage, contactName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentFirstContactEmailRoute)

        val view = application.injector.instanceOf[AgentFirstContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentContactDetailsNavigator].toInstance(new FakeAgentContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agentFirstContactEmailRoute)
            .withFormUrlEncodedBody(("value", "answer@b.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(AgentFirstContactNamePage, contactName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentFirstContactEmailRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentFirstContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }

  }
}

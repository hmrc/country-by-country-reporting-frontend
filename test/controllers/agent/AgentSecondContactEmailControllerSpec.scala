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
import forms.AgentSecondContactEmailFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{AgentContactDetailsNavigator, FakeAgentContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentSecondContactEmailPage, AgentSecondContactNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.agent.AgentSecondContactEmailView

import scala.concurrent.Future

class AgentSecondContactEmailControllerSpec extends SpecBase with MockitoSugar {

  val formProvider      = new AgentSecondContactEmailFormProvider()
  val form              = formProvider()
  val contactName       = "contact name"
  val contactNamePlural = "contact name’s"

  lazy val agentSecondContactEmailRoute = routes.AgentSecondContactEmailController.onPageLoad(NormalMode).url

  "AgentSecondContactEmail Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(AgentSecondContactNamePage, contactName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentSecondContactEmailRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentSecondContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AgentSecondContactNamePage, contactName)
        .success
        .value
        .set(AgentSecondContactEmailPage, "answer")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentSecondContactEmailRoute)

        val view = application.injector.instanceOf[AgentSecondContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentContactDetailsNavigator].toInstance(new FakeAgentContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agentSecondContactEmailRoute)
            .withFormUrlEncodedBody(("value", "email@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(AgentSecondContactNamePage, contactName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentSecondContactEmailRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentSecondContactEmailView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactNamePlural)(request, messages(application)).toString
      }
    }
  }
}

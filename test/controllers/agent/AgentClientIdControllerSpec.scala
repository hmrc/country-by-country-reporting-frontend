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

package controllers.agent

import base.SpecBase
import forms.AgentClientIdFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.agent.AgentClientIdView

import scala.concurrent.Future

class AgentClientIdControllerSpec extends SpecBase {
  val formProvider                    = new AgentClientIdFormProvider()
  val form                            = formProvider()
  lazy val agentClientIdRoute: String = routes.AgentClientIdController.onPageLoad().url

  "AgentClientIdController" - {

    "Must return OK and correct view for GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, agentClientIdRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientIdView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString()
      }
    }

    "Must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, agentClientIdRoute).withFormUrlEncodedBody(("value", "answer"))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.agent.routes.AgentIsThisYourClientController.onPageLoad.url
      }
    }

    "Must return a bad request, and errors, when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request   = FakeRequest(POST, agentClientIdRoute).withFormUrlEncodedBody(("value", ""))
        val result    = route(application, request).value
        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[AgentClientIdView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString()
      }
    }
  }
}

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
import pages.ContactNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.agent.AgentContactDetailsSavedView

class AgentContactDetailsSavedControllerSpec extends SpecBase {

  "AgentContactDetailsSaved Controller" - {

    "must return OK and the correct view for a GET when client contact details exist" in {

      val userAnswers = emptyUserAnswers.set(ContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsSavedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsSavedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientContactDetailsExist = true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when client contact details don not exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentContactDetailsSavedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentContactDetailsSavedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(clientContactDetailsExist = false)(request, messages(application)).toString
      }
    }
  }
}
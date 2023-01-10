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

package controllers

import base.SpecBase
import forms.SecondContactPhoneFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{ContactDetailsNavigator, FakeContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.{SecondContactNamePage, SecondContactPhonePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.SecondContactPhoneView

import scala.concurrent.Future

class SecondContactPhoneControllerSpec extends SpecBase {

  val formProvider = new SecondContactPhoneFormProvider()
  val form         = formProvider()

  private val name = "your second contact"

  lazy val secondContactPhoneRoute = routes.SecondContactPhoneController.onPageLoad(NormalMode).url

  "SecondContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(SecondContactNamePage, "CBC Company")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondContactPhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondContactPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, "CBC Company", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(SecondContactPhonePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondContactPhoneRoute)

        val view = application.injector.instanceOf[SecondContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), name, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

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
          FakeRequest(POST, secondContactPhoneRoute)
            .withFormUrlEncodedBody(("value", "99999"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondContactPhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, name, NormalMode)(request, messages(application)).toString
      }
    }
  }
}

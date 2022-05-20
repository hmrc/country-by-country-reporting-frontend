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
import forms.SecondContactHavePhoneFormProvider
import models.UserAnswers
import navigation.{ContactDetailsNavigator, FakeContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.{SecondContactHavePhonePage, SecondContactNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.SecondContactHavePhoneView

import scala.concurrent.Future

class SecondContactHavePhoneControllerSpec extends SpecBase with MockitoSugar {

  override def onwardRoute = Call("GET", "/foo")

  val formProvider = new SecondContactHavePhoneFormProvider()
  val form         = formProvider()

  private val name = "First Contact Name"

  lazy val secondContactHavePhoneRoute = routes.SecondContactHavePhoneController.onPageLoad().url

  "SecondContactHavePhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(SecondContactNamePage, name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondContactHavePhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondContactHavePhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, name)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(SecondContactNamePage, name)
        .success
        .value
        .set(SecondContactHavePhonePage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondContactHavePhoneRoute)

        val view = application.injector.instanceOf[SecondContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), name)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(SecondContactNamePage, name).success.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ContactDetailsNavigator].toInstance(new FakeContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, secondContactHavePhoneRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(SecondContactNamePage, name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, secondContactHavePhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondContactHavePhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, name)(request, messages(application)).toString
      }
    }

  }
}

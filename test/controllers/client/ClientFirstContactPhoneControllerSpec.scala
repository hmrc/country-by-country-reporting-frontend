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

package controllers.client

import base.SpecBase
import forms.ContactPhoneFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{ClientContactDetailsNavigator, FakeClientContactDetailsNavigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactNamePage, ContactPhonePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.client.ClientFirstContactPhoneView

import scala.concurrent.Future

class ClientFirstContactPhoneControllerSpec extends SpecBase with MockitoSugar {

  override def onwardRoute = Call("GET", "/foo")

  val formProvider = new ContactPhoneFormProvider()
  val form         = formProvider("clientFirstContactPhone")
  val contactName  = "first client contact name"

  lazy val clientFirstContactPhoneRoute = routes.ClientFirstContactPhoneController.onPageLoad(NormalMode).url

  "ClientFirstContactPhone Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(ContactNamePage, contactName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, clientFirstContactPhoneRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientFirstContactPhoneView]

        status(result) mustEqual OK
        val resultAsString = contentAsString(result)
        resultAsString mustEqual view(form, contactName, NormalMode)(request, messages(application)).toString
        val page         = Jsoup.parse(resultAsString)
        val labelElement = page.getElementsByTag("label").first()
        labelElement.html() mustBe "What is the telephone number for first client contact name?"
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ContactNamePage, contactName)
        .success
        .value
        .set(ContactPhonePage, "answer")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, clientFirstContactPhoneRoute)

        val view = application.injector.instanceOf[ClientFirstContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), contactName, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ClientContactDetailsNavigator].toInstance(new FakeClientContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, clientFirstContactPhoneRoute)
            .withFormUrlEncodedBody(("value", "0928273"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(ContactNamePage, contactName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, clientFirstContactPhoneRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ClientFirstContactPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, contactName, NormalMode)(request, messages(application)).toString
      }
    }

  }
}

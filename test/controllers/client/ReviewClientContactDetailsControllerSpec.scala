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
import forms.ReviewClientContactDetailsFormProvider
import models.subscription.{ContactInformation, OrganisationDetails}
import models.{NormalMode, UserAnswers}
import navigation.{ClientContactDetailsNavigator, FakeClientContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.{PrimaryClientContactInformationPage, ReviewClientContactDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.client.ReviewClientContactDetailsView

import scala.concurrent.Future

class ReviewClientContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider        = new ReviewClientContactDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  val contactInformation: ContactInformation = ContactInformation(OrganisationDetails("name"), "test@test.com", None, None)

  lazy val reviewClientContactDetailsRoute: String = routes.ReviewClientContactDetailsController.onPageLoad().url

  "ReviewClientContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PrimaryClientContactInformationPage, contactInformation)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, reviewClientContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReviewClientContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, contactInformation, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReviewClientContactDetailsPage, true)
        .success
        .value
        .set(PrimaryClientContactInformationPage, contactInformation)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reviewClientContactDetailsRoute)

        val view = application.injector.instanceOf[ReviewClientContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), contactInformation, NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, reviewClientContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PrimaryClientContactInformationPage, contactInformation)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, reviewClientContactDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReviewClientContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, contactInformation, NormalMode)(request, messages(application)).toString
      }
    }

  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.ReviewContactDetailsFormProvider
import models.UserAnswers
import models.subscription.{ContactInformation, OrganisationDetails}
import navigation.{ContactDetailsNavigator, FakeContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.{PrimaryClientContactInformationPage, ReviewContactDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import views.html.ReviewContactDetailsView

import scala.concurrent.Future

class ReviewContactDetailsControllerSpec extends SpecBase {

  val formProvider                                 = new ReviewContactDetailsFormProvider()
  val form: Form[Boolean]                          = formProvider()
  lazy val reviewContactDetailsRoute: String       = routes.ReviewContactDetailsController.onPageLoad().url
  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

  val migratedContactDetails: ContactInformation =
    ContactInformation(OrganisationDetails(organisationName = "ORGNAME"), email = "test@test.co.uk", phone = Some("12345678901"), mobile = None)

  val migratedAnswers: UserAnswers = emptyUserAnswers
    .withPage(
      PrimaryClientContactInformationPage,
      migratedContactDetails
    )
  when(mockSubscriptionService.getContactDetails(any(), any())(any()))
    .thenReturn(Future.successful(Some(migratedAnswers)))

  "ReviewContactDetails Controller" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, reviewContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReviewContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, migratedContactDetails)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReviewContactDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, reviewContactDetailsRoute)

        val view = application.injector.instanceOf[ReviewContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), migratedContactDetails)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ContactDetailsNavigator].toInstance(new FakeContactDetailsNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reviewContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, reviewContactDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReviewContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, migratedContactDetails)(request, messages(application)).toString
      }
    }

    "must redirect to problem page for a GET no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, reviewContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ThereIsAProblemController.onPageLoad().url
      }
    }

    "must redirect to problem page for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, reviewContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ThereIsAProblemController.onPageLoad().url
      }
    }
  }
}

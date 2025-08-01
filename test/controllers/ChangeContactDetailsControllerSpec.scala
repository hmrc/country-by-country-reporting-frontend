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

package controllers

import base.SpecBase
import models.UserAnswers
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ThereIsAProblemView

import scala.concurrent.Future

class ChangeContactDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

  override def beforeEach: Unit = {
    reset(mockSubscriptionService)
    super.beforeEach
  }

  "ChangeContactDetails Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET and show 'confirm and send' button on updating contact details" in {
        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, true)
          .withPage(ContactPhonePage, "7778889993")
          .withPage(HaveSecondContactPage, false)

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some((true, true))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Confirm and send").isEmpty mustBe false
        }
      }

      "must redirect to SomeInformationMissingPage when all mandatory values are missing" in {

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some((true, true))))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some("/send-a-country-by-country-report/problem/some-information-is-missing")
        }
      }

      "must redirect to SomeInformationMissingPage when any primary contact mandatory values are missing" in {

        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, true)
          .withPage(HaveSecondContactPage, false)

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some((true, true))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some("/send-a-country-by-country-report/problem/some-information-is-missing")
        }
      }

      "must redirect to SomeInformationMissingPage when any secondary contact mandatory values are missing" in {

        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, false)
          .withPage(HaveSecondContactPage, true)

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some((true, true))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some("/send-a-country-by-country-report/problem/some-information-is-missing")
        }
      }

      "must return OK and the correct view for a GET and hide 'confirm and send' button on not updating contact details" in {

        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, true)
          .withPage(ContactPhonePage, "7778889993")
          .withPage(HaveSecondContactPage, false)

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some((false, false))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Confirm and send").isEmpty mustBe true
        }
      }

      "must load 'Internal server error' page on failing to read subscription details" in {

        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, true)
          .withPage(ContactPhonePage, "7778889993")
          .withPage(HaveSecondContactPage, false)

        when(mockSubscriptionService.isContactInformationUpdated(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {

      "redirect to confirmation page on updating ContactDetails" in {
        when(mockSubscriptionService.updateContactDetails(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ChangeContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.DetailsUpdatedController.onPageLoad().url
        }
      }

      "load 'technical difficulties' page on failing to update ContactDetails" in {
        when(mockSubscriptionService.updateContactDetails(any[UserAnswers], any[String])(any[HeaderCarrier]()))
          .thenReturn(Future.successful(false))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ChangeContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ThereIsAProblemView]

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }

    }
  }
}

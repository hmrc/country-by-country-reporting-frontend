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
import models.UserAnswers
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AgentSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ChangeAgentContactDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockAgentSubscriptionService: AgentSubscriptionService = mock[AgentSubscriptionService]

  override def beforeEach: Unit = {
    reset(mockAgentSubscriptionService)
    super.beforeEach
  }

  "changeAgentContactDetails Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET and show 'confirm and send' button on updating contact details" in {

        when(mockAgentSubscriptionService.isAgentContactInformationUpdated(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeAgentContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Confirm and send").isEmpty mustBe false
        }
      }

      "must return OK and the correct view for a GET and hide 'confirm and send' button on not updating contact details" in {

        when(mockAgentSubscriptionService.isAgentContactInformationUpdated(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(false)))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeAgentContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Confirm and send").isEmpty mustBe true
        }
      }

      "must return OK and the correct view for a GET and show 'Back to send a CBC report' link if contact details exist (change journey)" in {

        when(mockAgentSubscriptionService.isAgentContactInformationUpdated(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeAgentContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Back to manage your country-by-country report").isEmpty mustBe false
        }
      }

      "must return OK and the correct view for a GET and hide 'Back to send a CBC report' link if contact details don't exist (create journey)" in {

        when(mockAgentSubscriptionService.isAgentContactInformationUpdated(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(false)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeAgentContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Back to send a CBC report").isEmpty mustBe true
        }
      }

      "must load 'Internal server error' page on failing to read subscription details" in {

        when(mockAgentSubscriptionService.isAgentContactInformationUpdated(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ChangeAgentContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }

    "onSubmit" - {

      "redirect to confirmation page on saving new agent ContactDetails on first visit" in {
        when(mockAgentSubscriptionService.createAgentContactDetails(any[String], any[UserAnswers])(any[HeaderCarrier]))
          .thenReturn(Future.successful(true))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(false)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ChangeAgentContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AgentContactDetailsSavedController.onPageLoad().url
        }
      }

      "redirect to confirmation page on updating existing agent ContactDetails" in {
        when(mockAgentSubscriptionService.updateAgentContactDetails(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(true))

        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ChangeAgentContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AgentContactDetailsUpdatedController.onPageLoad().url
        }
      }

      "load 'technical difficulties' page on failing to update agent ContactDetails" in {
        when(mockAgentSubscriptionService.updateAgentContactDetails(any[UserAnswers]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(false))
        when(mockAgentSubscriptionService.doAgentContactDetailsExist()(any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(true)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ChangeAgentContactDetailsController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

    }
  }
}

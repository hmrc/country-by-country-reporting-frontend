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
import connectors.FileDetailsConnector
import controllers.actions.{
  DataRequiredAction,
  DataRequiredActionImpl,
  DataRetrievalAction,
  FakeAgentIdentifierAction,
  FakeDataRetrievalActionProvider,
  IdentifierAction
}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import pages.HaveTelephonePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AgentSubscriptionService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexControllerSpec extends SpecBase {

  "Index Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockSubscriptionService  = mock[SubscriptionService]
      val mockFileDetailsConnector = mock[FileDetailsConnector]

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      val userAnswers = UserAnswers("id").set(HaveTelephonePage, false).success.value
      when(mockSubscriptionService.getContactDetails(any[UserAnswers], any[String])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(userAnswers)))
      when(mockFileDetailsConnector.getAllFileDetails(any[String])(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(None))
      when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(showRecentFiles = false, "subscriptionId")(request, messages(application)).toString
      }
    }

    "must return SEE_OTHER and redirect to 'Contact details needed' page for returning user after migration" in {
      val userAnswers             = UserAnswers("id")
      val mockSubscriptionService = mock[SubscriptionService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSubscriptionService.getContactDetails(any[UserAnswers], any[String])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ContactDetailsNeededController.onPageLoad().url)
      }
    }

    "must return SEE_OTHER and redirect to 'Agent Contact details needed' page for new agent on first visit" in {
      val userAnswers                  = UserAnswers("id")
      val mockSubscriptionService      = mock[SubscriptionService]
      val mockAgentSubscriptionService = mock[AgentSubscriptionService]

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeAgentIdentifierAction],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(userAnswers))),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockAgentSubscriptionService.getAgentContactDetails(any[UserAnswers]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(userAnswers)))

      when(mockSubscriptionService.getContactDetails(any[UserAnswers], any[String])(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.agent.routes.AgentContactDetailsNeededController.onPageLoad().url)
      }
    }
  }
}

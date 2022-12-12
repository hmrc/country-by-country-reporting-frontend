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
import forms.AgentIsThisYourClientFormProvider
import models.UserAnswers
import navigation.{AgentContactDetailsNavigator, FakeAgentContactDetailsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.AgentIsThisYourClientPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.agent.AgentIsThisYourClientView

import scala.concurrent.Future

class AgentIsThisYourClientControllerSpec extends SpecBase {

  val formProvider = new AgentIsThisYourClientFormProvider()
  val form         = formProvider()
  val tradingName  = "exampleTradingName"
  val clientId     = "subscriptionId"

  lazy val agentIsThisYourClientRoute = routes.AgentIsThisYourClientController.onPageLoad.url

  "AgentIsThisYourClient Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      when(mockSubscriptionService.getTradingNames(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Some(tradingName)))

      running(application) {
        val request = FakeRequest(GET, agentIsThisYourClientRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentIsThisYourClientView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, clientId, tradingName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

      val userAnswers = emptyUserAnswers
        .set(AgentIsThisYourClientPage, value = true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      when(mockSubscriptionService.getTradingNames(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Some(tradingName)))

      running(application) {
        val request = FakeRequest(GET, agentIsThisYourClientRoute)

        val view = application.injector.instanceOf[AgentIsThisYourClientView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), clientId, tradingName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]

      val userAnswers = emptyUserAnswers
        .set(AgentIsThisYourClientPage, value = true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      when(mockSubscriptionService.getTradingNames(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(Some(tradingName)))

      running(application) {
        val request =
          FakeRequest(POST, agentIsThisYourClientRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    /*
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentIsThisYourClientRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentIsThisYourClientView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, agentIsThisYourClientRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, agentIsThisYourClientRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }*/
  }
}

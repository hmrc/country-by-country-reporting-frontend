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
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IndexView

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockSubscriptionService = mock[SubscriptionService]

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      val userAnswers = UserAnswers("id")
      when(mockSubscriptionService.getContactDetails(any[UserAnswers]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}

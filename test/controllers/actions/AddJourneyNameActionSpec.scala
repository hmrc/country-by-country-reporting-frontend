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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.scalatest.Inside.inside
import pages.{ContactDetailsJourneyTypePage, JourneyInProgressPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Agent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddJourneyNameActionSpec extends SpecBase {

  class Harness(sessionRepository: SessionRepository) extends AddJourneyNameActionProvider("some-journey", sessionRepository) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Add Journey TypeAction" - {

    "must update userAnswer with a journey Name" in {
      val sessionRepository = mock[SessionRepository]
      val userAnswers       = UserAnswers(userAnswersId).withPage(JourneyInProgressPage, true)

      when(sessionRepository.set(any())).thenReturn(Future(true))
      val action = new Harness(sessionRepository)

      val result = action.callRefine(DataRequest(FakeRequest(), "id", "subscriptionId", Agent, userAnswers, None)).futureValue

      inside(result) {
        case Right(value) =>
          value.userAnswers.get(ContactDetailsJourneyTypePage).get mustBe "some-journey"
      }
    }
  }
}

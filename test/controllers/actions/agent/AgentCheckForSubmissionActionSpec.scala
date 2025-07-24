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

package controllers.actions.agent

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.AgentDataRequest
import org.scalatest.EitherValues
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.{AgentFirstContactEmailPage, AgentFirstContactNamePage, AgentFirstContactPhonePage, ContactEmailPage, ContactPhonePage, JourneyInProgressPage}
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentCheckForSubmissionActionSpec extends SpecBase with EitherValues {

  class Harness extends AgentCheckForSubmissionActionImpl {
    def callRefine[A](request: AgentDataRequest[A]): Future[Either[Result, AgentDataRequest[A]]] = super.refine(request)
  }

  "CheckForSubmission Action" - {

    "when there is no flag set for contact details journeys" - {

      "must redirect to already submitted page" in new TestContext {

        val action = new Harness

        val result = action.callRefine(AgentDataRequest(FakeRequest(), "id", emptyUserAnswers, "ARN")).map(_.left.value)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad().url
      }
    }

    "when there is a flag set for contact details journeys" - {

      "must allow the user to continue" in new TestContext {

        val action = new Harness
        val result = action.callRefine(AgentDataRequest(FakeRequest(), "id", userAnswersWithAllDetails, "ARN")).futureValue

        result.isRight mustBe true
      }
    }

    "when agents mandatory contact details are missing" - {
      "must redirect to missing information page when mandatory details are missing" in new TestContext {
        forAll(testCases) {
          testUserAnswers =>
            emptyUserAnswers
            val action = new Harness
            val result = action.callRefine(AgentDataRequest(FakeRequest(), "id", testUserAnswers, "ARN")).map(_.left.value)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustEqual routes.SomeInformationMissingController.onPageLoad().url
        }
      }
    }

    "when agent mandatory contact details are present" - {
      "must allow the user to continue" in new TestContext {
        val action = new Harness

        val result = action.callRefine(AgentDataRequest(FakeRequest(), "id", userAnswersWithAllDetails, "ARN")).futureValue

        result.isRight mustBe true
      }
    }
  }

  trait TestContext extends TableDrivenPropertyChecks {

    val userAnswersWithEmail       = UserAnswers(userAnswersId).withPage(AgentFirstContactEmailPage, "email@example.com").withPage(JourneyInProgressPage, true)
    val userAnswersWithContactName = UserAnswers(userAnswersId).withPage(AgentFirstContactNamePage, "Some Agent Name").withPage(JourneyInProgressPage, true)

    val userAnswersWithAllDetails = UserAnswers(userAnswersId)
      .withPage(AgentFirstContactNamePage, "Some Agent Name")
      .withPage(AgentFirstContactEmailPage, "email@example.com")
      .withPage(JourneyInProgressPage, true)

    val testCases = Table(
      "userAnswers",
      userAnswersWithEmail,
      userAnswersWithContactName
    )
  }
}

/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.routes
import models.requests.DataRequest
import models.upscan.UploadId
import org.scalatest.EitherValues
import pages.{JourneyInProgressPage, UploadIDPage}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckForSubmissionActionSpec extends SpecBase with EitherValues {

  class Harness(checkFileSubmission: Boolean) extends CheckForSubmissionActionProvider(checkFileSubmission) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = super.refine(request)
  }

  "CheckForSubmission Action" - {

    "when there is no flag set for contact details journeys" - {

      "must redirect to already submitted page" in {

        val action = new Harness(false)

        val result = action.callRefine(DataRequest(FakeRequest(), "id", "subscriptionId", Organisation, emptyUserAnswers)).map(_.left.value)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad().url
      }
    }

    "when there is a flag set for contact details journeys" - {

      "must allow the user to continue" in {

        val action = new Harness(false)

        val userAnswers = emptyUserAnswers.set(JourneyInProgressPage, true).success.value
        val result      = action.callRefine(DataRequest(FakeRequest(), "id", "subscriptionId", Organisation, userAnswers)).futureValue

        result.isRight mustBe true
      }
    }

    "when there is no flag set for file upload journeys" - {

      "must redirect to already submitted page" in {

        val action = new Harness(true)

        val result = action.callRefine(DataRequest(FakeRequest(), "id", "subscriptionId", Organisation, emptyUserAnswers)).map(_.left.value)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.InformationSentController.onPageLoad().url
      }
    }

    "when there is a flag set for file upload journeys" - {

      "must allow the user to continue" in {

        val action = new Harness(true)

        val userAnswers = emptyUserAnswers.set(UploadIDPage, UploadId("id")).success.value
        val result      = action.callRefine(DataRequest(FakeRequest(), "id", "subscriptionId", Organisation, userAnswers)).futureValue

        result.isRight mustBe true
      }
    }
  }
}

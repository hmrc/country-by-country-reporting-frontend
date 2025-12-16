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

package connectors

import models.submission.SubmissionDetails
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, REQUEST_TIMEOUT}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class SubmissionConnectorSpec extends Connector {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.country-by-country-reporting.port" -> server.port())
    .build()

  val submitUrl = "/country-by-country-reporting/submit"

  "SubmissionConnector" - {

    "must return a 200 on successful submission" in {
      lazy val connector: SubmissionConnector = inject[SubmissionConnector]

      forAll { (submissionDetails: SubmissionDetails) =>
        stubPostResponse(submitUrl, OK, Json.toJson(conversationId).toString())

        connector.submitDocument(submissionDetails).futureValue.value mustBe conversationId
      }
    }

    "must return a 400 when submission fails with BadRequest" in {
      lazy val connector: SubmissionConnector = inject[SubmissionConnector]

      forAll { (submissionDetails: SubmissionDetails) =>
        stubPostResponse(submitUrl, BAD_REQUEST)

        connector.submitDocument(submissionDetails).futureValue mustBe None
      }
    }

    "must return a 500 when submission fails with InternalServer Error" in {
      lazy val connector: SubmissionConnector = inject[SubmissionConnector]

      forAll { (submissionDetails: SubmissionDetails) =>
        stubPostResponse(submitUrl, INTERNAL_SERVER_ERROR)

        connector.submitDocument(submissionDetails).futureValue mustBe None
      }
    }

    "must return None when submission fails with Request Timeout" in {
      lazy val connector: SubmissionConnector = inject[SubmissionConnector]
      forAll { (submissionDetails: SubmissionDetails) =>
        stubPostResponse(submitUrl, REQUEST_TIMEOUT)

        connector.submitDocument(submissionDetails).futureValue mustBe None
      }
    }
  }

}

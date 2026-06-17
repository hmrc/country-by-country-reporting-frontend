/*
 * Copyright 2026 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.upscan.FileValidateRequest
import models.{
  CBC401,
  GenericError,
  Message,
  MessageSpecData,
  NonFatalErrors,
  SubmissionValidationFailure,
  SubmissionValidationSuccess,
  TestData,
  ValidationErrors
}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ValidationConnectorSpec extends Connector {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(getWireMockAppConfig(Seq("country-by-country-reporting")))
      .build()

  private def connector: ValidationConnector = app.injector.instanceOf[ValidationConnector]

  private val validateUrl = "/country-by-country-reporting/validate-submission"

  private val fileValidateRequest =
    FileValidateRequest(url = "url", conversationId = "conversationId", subscriptionId = "subscriptionId", fileReferenceId = "fileReferenceId")

  private val messageSpecData = MessageSpecData("messageRefId", CBC401, TestData, LocalDate.of(2012, 1, 1), LocalDate.of(2016, 1, 1), "testReportingEntity")

  "ValidationConnector" - {

    "sendForValidation" - {

      "must return Right(MessageSpecData) when the backend responds with 200 and a SubmissionValidationSuccess" in {
        val body = Json.toJson(SubmissionValidationSuccess(messageSpecData)).toString()
        stubPostResponse(validateUrl, 200, body)

        val result = connector.sendForValidation(fileValidateRequest).futureValue

        result mustBe Right(messageSpecData)
      }

      "must return Left(validationErrors) when the backend responds with 200 and a SubmissionValidationFailure" in {
        val validationErrors = ValidationErrors(Seq(GenericError(1, Message("testError"))), None)
        val body             = Json.toJson(SubmissionValidationFailure(validationErrors)).toString()

        stubPostResponse(validateUrl, 200, body)

        val result = connector.sendForValidation(fileValidateRequest).futureValue

        result mustBe Left(validationErrors)
      }

      "must return Left(InvalidXmlError) when the backend throws an exception containing 'Invalid XML'" in {
        server.stubFor(
          post(urlEqualTo(validateUrl))
            .willReturn(
              aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.MALFORMED_RESPONSE_CHUNK)
            )
        )

        val result = connector.sendForValidation(fileValidateRequest).futureValue

        result.isLeft mustBe true
      }
    }
  }
}

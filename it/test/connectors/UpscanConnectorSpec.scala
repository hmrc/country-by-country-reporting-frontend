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

package connectors

import models.upscan._
import org.bson.types.ObjectId
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.UpstreamErrorResponse

class UpscanConnectorSpec extends Connector {

  val uploadId: UploadId = UploadId("12345")

  private val FileSize = 20L

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.upscan.port"                       -> server.port(),
      "microservice.services.country-by-country-reporting.port" -> server.port()
    )
    .build()

  val request: UpscanInitiateRequest = UpscanInitiateRequest("callbackUrl")

  "getUpscanFormData" - {
    "should return an UpscanInitiateResponse" - {
      "when upscan returns a valid successful response" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body = PreparedUpload(Reference("Reference"), UploadForm("downloadUrl", Map("formKey" -> "formValue")))

        stubPostResponse(connector.upscanInitiatePath, OK, Json.toJson(body).toString())

        whenReady(connector.getUpscanFormData(uploadId)) {
          result =>
            result mustBe body.toUpscanInitiateResponse
        }

      }
    }

    "throw an exception" - {
      "when upscan returns a 4xx response" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        stubPostResponse(connector.upscanInitiatePath, BAD_REQUEST)

        val result = connector.getUpscanFormData(uploadId)

        whenReady(result.failed) {
          e =>
            e mustBe an[UpstreamErrorResponse]
            val error = e.asInstanceOf[UpstreamErrorResponse]
            error.statusCode mustBe BAD_REQUEST
        }
      }

      "when upscan returns 5xx response" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        stubPostResponse(connector.upscanInitiatePath, SERVICE_UNAVAILABLE)

        val result = connector.getUpscanFormData(uploadId)
        whenReady(result.failed) {
          e =>
            e mustBe an[UpstreamErrorResponse]
            val error = e.asInstanceOf[UpstreamErrorResponse]
            error.statusCode mustBe SERVICE_UNAVAILABLE
        }
      }
    }
  }

  "getUploadDetails" - {
    "should return an UploadSessionDetails" - {
      "when a valid successful response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body = UploadSessionDetails(_id = ObjectId.get(),
                                        uploadId = UploadId("12345"),
                                        reference = Reference("Reference"),
                                        status = UploadedSuccessfully("name", "downloadUrl", FileSize, "MD5:123")
        )

        stubGetResponse("/country-by-country-reporting/upscan/details/12345", OK, Json.toJson(body).toString())

        whenReady(connector.getUploadDetails(uploadId)) {
          result =>
            result mustBe Some(body)
        }

      }
    }

    "should return None" - {
      "when an invalid response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        stubGetResponse("/country-by-country-reporting/upscan/details/12345", OK, Json.obj().toString())

        whenReady(connector.getUploadDetails(uploadId)) {
          result =>
            result mustBe None
        }

      }
    }
  }

  "getUploadStatus" - {
    "should return an UploadStatus for a valid UploadId" - {
      "when an UploadedSuccessfully response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body =
          """{
            | "_type": "UploadedSuccessfully",
            | "name": "name",
            | "downloadUrl": "downloadUrl",
            | "size": 20,
            | "checksum": "MD5:123"
            | }
            |""".stripMargin

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, body)

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe Some(UploadedSuccessfully("name", "downloadUrl", FileSize, "MD5:123"))
        }
      }

      "when a NotStarted response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body =
          """{
            | "_type": "NotStarted"
            | }
            |""".stripMargin

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, body)

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe Some(NotStarted)
        }
      }

      "when a InProgress response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body =
          """{
            | "_type": "InProgress"
            | }
            |""".stripMargin

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, body)

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe Some(InProgress)
        }
      }

      "when a Failed response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body =
          """{
            | "_type": "Failed"
            | }
            |""".stripMargin

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, body)

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe Some(Failed)
        }
      }

      "when a Quarantined response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        val body =
          """{
            | "_type": "Quarantined"
            | }
            |""".stripMargin

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, body)

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe Some(Quarantined)
        }
      }
    }

    "should return None" - {
      "when an invalid response is returned" in {
        lazy val connector: UpscanConnector = inject[UpscanConnector]

        stubGetResponse("/country-by-country-reporting/upscan/status/12345", OK, Json.obj().toString())

        whenReady(connector.getUploadStatus(uploadId)) {
          result =>
            result mustBe None
        }

      }
    }
  }
}

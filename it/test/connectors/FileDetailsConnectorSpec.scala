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

import models.fileDetails.BusinessRuleErrorCode.{MessageRefIDHasAlreadyBeenUsed, MessageTypeIndic}
import models.fileDetails._
import models.{fileDetails, ConversationId, NewInformation}
import play.api.Application
import play.api.http.Status.{OK, REQUEST_TIMEOUT}
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class FileDetailsConnectorSpec extends Connector {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.country-by-country-reporting.port" -> server.port()
    )
    .build()

  "FileDetailsConnector" - {

    "getAllFileDetails" - {

      "must return 'all file details' when getAllFileDetails is successful" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val expectedResult = Some(
          Seq(
            FileDetails(
              "test1.xml",
              "messageRefId1",
              "Reporting Entity",
              NewInformation,
              LocalDateTime.parse("2022-02-10T15:35:37.636"),
              LocalDateTime.parse("2022-02-10T15:35:37.636"),
              Pending,
              ConversationId("conversationId1")
            ),
            fileDetails.FileDetails(
              "test2.xml",
              "messageRefId2",
              "Reporting Entity",
              NewInformation,
              LocalDateTime.parse("2022-02-10T15:35:37.636"),
              LocalDateTime.parse("2022-02-10T15:45:37.636"),
              Rejected(
                FileValidationErrors(
                  Some(List(FileErrors(MessageRefIDHasAlreadyBeenUsed, Some("Duplicate message ref ID")))),
                  Some(
                    List(
                      RecordError(
                        MessageTypeIndic,
                        Some(
                          "A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both"
                        ),
                        Some(List("asjdhjjhjssjhdjshdAJGSJJS"))
                      )
                    )
                  )
                )
              ),
              ConversationId("conversationId2")
            )
          )
        )

        stubGetResponse(allFilesUrls, OK, allFiles)

        val result = connector.getAllFileDetails(cbcId)

        result.futureValue mustBe expectedResult
      }

      "must return 'None' when getAllFileDetails is successful but response json is invalid" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubGetResponse(allFilesUrls, OK)

        val result = connector.getAllFileDetails(cbcId)

        result.futureValue mustBe None
      }

      "must return 'None' when getAllFileDetails fails with Error" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val errorCode = errorCodes.sample.value
        stubGetResponse(allFilesUrls, errorCode)

        val result = connector.getAllFileDetails(cbcId)

        result.futureValue mustBe None

      }

      "must return 'None' when getAllFileDetails fails with request timeout" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubGetResponse(allFilesUrls, REQUEST_TIMEOUT)

        val result = connector.getAllFileDetails(cbcId)

        result.futureValue mustBe None

      }
    }

    "getFileDetails" - {

      "must return 'file details' when getFileDetails is successful" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val expectedResult = Some(
          fileDetails.FileDetails(
            "test3.xml",
            "messageRefId3",
            "Reporting Entity",
            NewInformation,
            LocalDateTime.parse("2022-02-10T15:35:37.636"),
            LocalDateTime.parse("2022-02-10T15:45:37.636"),
            Accepted,
            ConversationId("conversationId3")
          )
        )

        stubGetResponse(fileUrl, OK, file)

        val result = connector.getFileDetails(conversationId)

        result.futureValue mustBe expectedResult
      }

      "must return 'None' when getFileDetails is successful but response json is invalid" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubPostResponse(fileUrl, OK)

        val result = connector.getFileDetails(conversationId)

        result.futureValue mustBe None
      }

      "must return 'None' when getFileDetails fails with Error" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val errorCode = errorCodes.sample.value
        stubPostResponse(fileUrl, errorCode)

        val result = connector.getFileDetails(conversationId)

        result.futureValue mustBe None

      }

      "must return 'None' when getFileDetails fails with Request Timeout" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubPostResponse(fileUrl, REQUEST_TIMEOUT)

        val result = connector.getFileDetails(conversationId)

        result.futureValue mustBe None

      }
    }

    "getStatus" - {

      "must return 'file status' when getStatus is successful" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val expectedResult = Some(Accepted)

        stubGetResponse(fileStatusUrl, OK, acceptedFileStatus)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe expectedResult
      }

      "must return 'None' when getStatus is successful but response json is invalid" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubPostResponse(fileStatusUrl, OK)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe None
      }

      "must return 'None' when getStatus fails with Error" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        val errorCode = errorCodes.sample.value
        stubPostResponse(fileStatusUrl, errorCode)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe None

      }

      "must return 'None' when getStatus fails with Request Timeout" in {
        lazy val connector: FileDetailsConnector = inject[FileDetailsConnector]

        stubPostResponse(fileStatusUrl, REQUEST_TIMEOUT)

        val result = connector.getStatus(conversationId)

        result.futureValue mustBe None

      }
    }

  }

}

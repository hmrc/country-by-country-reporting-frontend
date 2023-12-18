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

package controllers

import base.SpecBase
import connectors.FileDetailsConnector
import generators.ModelGenerators
import models.ConversationId
import models.fileDetails.{Accepted, FileDetails, FileValidationErrors, Rejected}
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.FileRejectedViewModel
import views.html.{FileRejectedView, ThereIsAProblemView}

import java.time.LocalDateTime
import scala.concurrent.Future

class FileRejectedControllerSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {

  private val fileName       = "TestFile.xml"
  private val conversationId = ConversationId("conversationId")
  private val messageRefId   = "messageRefId"

  "FileRejected Controller" - {

    val mockFileDetailsConnector: FileDetailsConnector = mock[FileDetailsConnector]

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      val validationErrors = Arbitrary.arbitrary[FileValidationErrors].sample.value

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(
              FileDetails(
                fileName,
                messageRefId,
                "Reporting Entity",
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                Rejected(validationErrors),
                conversationId
              )
            )
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.FileRejectedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileRejectedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileName, FileRejectedViewModel(validationErrors))(request, messages(application)).toString
      }
    }

    "must return ThereIsAProblemPage on failing to get FileDetails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any())).thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, routes.FileRejectedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must return ThereIsAProblemPage on getting FileDetails with a status other than Rejected" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(
              FileDetails(
                fileName,
                messageRefId,
                "Reporting Entity",
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                Accepted,
                conversationId
              )
            )
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.FileRejectedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}

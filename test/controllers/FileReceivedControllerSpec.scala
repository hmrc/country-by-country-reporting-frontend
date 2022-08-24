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
import connectors.FileDetailsConnector
import models.ConversationId
import models.fileDetails.{Accepted, FileDetails}
import org.mockito.ArgumentMatchers.any
import pages.{ContactEmailPage, SecondContactEmailPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.FileReceivedView

import java.time.LocalDateTime
import scala.concurrent.Future

class FileReceivedControllerSpec extends SpecBase {

  val mockFileDetailsConnector: FileDetailsConnector = mock[FileDetailsConnector]

  "FileReceived Controller" - {

    "must return OK and the correct view for a GET" in {

      val messageRefId       = "messageRefId"
      val conversationId     = ConversationId("conversationId")
      val time               = "10:30am"
      val date               = "1 January 2022"
      val firstContactEmail  = "first@email.com"
      val secondContactEmail = "second@email.com"

      val userAnswers = emptyUserAnswers
        .set(ContactEmailPage, firstContactEmail)
        .success
        .value
        .set(SecondContactEmailPage, secondContactEmail)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(
              FileDetails(
                "name",
                messageRefId,
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                LocalDateTime.parse("2022-01-01T10:30:00.000"),
                Accepted,
                conversationId
              )
            )
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.FileReceivedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReceivedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(messageRefId, time, date, firstContactEmail, Some(secondContactEmail))(request, messages(application)).toString
      }
    }
  }
}

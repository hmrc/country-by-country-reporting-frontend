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

package controllers

import base.SpecBase
import connectors.FileDetailsConnector
import controllers.actions._
import models.{CBC401, ConversationId, MessageSpecData, TestData, ValidatedFileData}
import models.fileDetails.{Accepted, FileDetails}
import org.mockito.ArgumentMatchers.any
import pages.{AgentFirstContactEmailPage, AgentSecondContactEmailPage, ContactEmailPage, SecondContactEmailPage, ValidXMLPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.FileReceivedViewModel
import views.html.{FileReceivedAgentView, FileReceivedView}
import viewmodels.govuk.summarylist._

import java.time.LocalDateTime
import scala.concurrent.Future

class FileReceivedControllerSpec extends SpecBase {

  private val FileSize = 20L

  val mockFileDetailsConnector: FileDetailsConnector = mock[FileDetailsConnector]

  "FileReceived Controller" - {

    val messageRefId            = "messageRefId"
    val conversationId          = ConversationId("conversationId")
    val firstContactEmail       = "first@email.com"
    val secondContactEmail      = "second@email.com"
    val agentFirstContactEmail  = "agentfirst@email.com"
    val agentSecondContactEmail = "agentsecond@email.com"
    val vfd: ValidatedFileData = ValidatedFileData(
      "filename.xml",
      MessageSpecData("messageRefId", CBC401, TestData, startDate, endDate, "Reporting Entity"),
      FileSize,
      "MD5:123"
    )

    val userAnswers = emptyUserAnswers
      .set(ContactEmailPage, firstContactEmail)
      .success
      .value
      .set(SecondContactEmailPage, secondContactEmail)
      .success
      .value
      .set(ValidXMLPage, vfd)
      .success
      .value

    val agentUserAnswers = userAnswers
      .set(AgentFirstContactEmailPage, agentFirstContactEmail)
      .success
      .value
      .set(AgentSecondContactEmailPage, agentSecondContactEmail)
      .success
      .value

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      val fileDetails = FileDetails(
        "name",
        messageRefId,
        "Reporting Entity",
        TestData,
        LocalDateTime.parse("2022-01-01T10:30:00.000"),
        LocalDateTime.parse("2022-01-01T10:30:00.000"),
        Accepted,
        conversationId
      )

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(
              fileDetails
            )
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.FileReceivedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReceivedView]

        val list = SummaryListViewModel(FileReceivedViewModel.getSummaryRows(fileDetails)(messages(application)))
          .withMargin()

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          list,
          "10:30am",
          "1 January 2022",
          firstContactEmail,
          Some(secondContactEmail)
        )(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET for Agent" in {

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeIdentifierActionAgent],
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector),
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(agentUserAnswers)))
        )
        .build()

      val fileDetails = FileDetails(
        "name",
        messageRefId,
        "Reporting Entity",
        TestData,
        LocalDateTime.parse("2022-01-01T10:30:00.000"),
        LocalDateTime.parse("2022-01-01T10:30:00.000"),
        Accepted,
        conversationId
      )

      when(mockFileDetailsConnector.getFileDetails(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(
              fileDetails
            )
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.FileReceivedController.onPageLoad(conversationId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileReceivedAgentView]

        val list = SummaryListViewModel(FileReceivedViewModel.getAgentSummaryRows(fileDetails)(messages(application)))
          .withMargin()

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          list,
          "10:30am",
          "1 January 2022",
          firstContactEmail,
          Some(secondContactEmail),
          agentFirstContactEmail,
          Some(agentSecondContactEmail)
        )(
          request,
          messages(application)
        ).toString
      }
    }
  }
}

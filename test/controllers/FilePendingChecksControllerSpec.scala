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
import controllers.actions._
import models.fileDetails.FileErrorCode.{FailedSchemaValidation, MessageRefIDHasAlreadyBeenUsed}
import models.fileDetails.RecordErrorCode.{DocRefIDFormat, MissingCorrDocRefId}
import models.fileDetails.{Pending, Rejected, ValidationErrors, Accepted => FileStatusAccepted, _}
import models.{CBC401, ConversationId, MessageSpecData, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentMatchers.any
import pages.{ConversationIdPage, ValidXMLPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.FileCheckViewModel
import views.html.FilePendingChecksView

import scala.concurrent.Future

class FilePendingChecksControllerSpec extends SpecBase {

  "FilePendingChecks Controller" - {

    val mockFileDetailsConnector: FileDetailsConnector = mock[FileDetailsConnector]
    val conversationId                                 = ConversationId("conversationId")

    "must return OK and the correct view for a GET when fileStatus is Pending" in {

      val validXmlDetails = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401, "Reporting Entity"))
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(ConversationIdPage, conversationId)
        .success
        .value
        .set(ValidXMLPage, validXmlDetails)
        .success
        .value

      when(mockFileDetailsConnector.getStatus(any())(any(), any())).thenReturn(Future.successful(Some(Pending)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      val fileSummaryList = FileCheckViewModel.createFileSummary(validXmlDetails.fileName, "Pending")(messages(application))
      val action          = routes.FilePendingChecksController.onPageLoad().url

      running(application) {

        val request = FakeRequest(GET, routes.FilePendingChecksController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FilePendingChecksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileSummaryList, action, "conversationId", false)(request, messages(application)).toString
      }
    }

    "must return OK for Agent and the correct view for a GET when fileStatus is Pending" in {

      val validXmlDetails = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401, "Reporting Entity"))
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(ConversationIdPage, conversationId)
        .success
        .value
        .set(ValidXMLPage, validXmlDetails)
        .success
        .value

      when(mockFileDetailsConnector.getStatus(any())(any(), any())).thenReturn(Future.successful(Some(Pending)))

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeAgentIdentifierAction],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(userAnswers))),
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      val fileSummaryList = FileCheckViewModel.createFileSummary(validXmlDetails.fileName, "Pending")(messages(application))
      val action          = routes.FilePendingChecksController.onPageLoad().url

      running(application) {

        val request = FakeRequest(GET, routes.FilePendingChecksController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FilePendingChecksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileSummaryList, action, "conversationId", true)(request, messages(application)).toString
      }
    }

    "must redirect to File Problem Page when REJECTED status returned with 'problem' errors" in {

      val validXmlDetails  = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401, "Reporting Entity"))
      val validationErrors = ValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(ConversationIdPage, conversationId)
        .success
        .value
        .set(ValidXMLPage, validXmlDetails)
        .success
        .value

      when(mockFileDetailsConnector.getStatus(any())(any(), any())).thenReturn(Future.successful(Some(Rejected(validationErrors))))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.FilePendingChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.FileProblemController.onPageLoad().url
      }
    }

    "must redirect to File Problem Page when REJECTED status returned with regular errors" in {

      val validXmlDetails = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401, "Reporting Entity"))
      val validationErrors =
        ValidationErrors(Some(Seq(FileErrors(MessageRefIDHasAlreadyBeenUsed, None))), Some(Seq(RecordError(MissingCorrDocRefId, None, None))))

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(ConversationIdPage, conversationId)
        .success
        .value
        .set(ValidXMLPage, validXmlDetails)
        .success
        .value

      when(mockFileDetailsConnector.getStatus(any())(any(), any())).thenReturn(Future.successful(Some(Rejected(validationErrors))))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.FilePendingChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.FileFailedChecksController.onPageLoad().url
      }
    }

    "must redirect to File Passed Checks Page when ACCEPTED status returned" in {

      val validXmlDetails = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401, "Reporting Entity"))

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(ConversationIdPage, conversationId)
        .success
        .value
        .set(ValidXMLPage, validXmlDetails)
        .success
        .value

      when(mockFileDetailsConnector.getStatus(any())(any(), any())).thenReturn(Future.successful(Some(FileStatusAccepted)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.FilePendingChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.FilePassedChecksController.onPageLoad().url
      }
    }
  }
}

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
import config.FrontendAppConfig
import connectors.{FileDetailsConnector, SubmissionConnector}
import generators.Generators
import models.fileDetails.BusinessRuleErrorCode.{DocRefIDFormat, InvalidMessageRefIDFormat, UnknownErrorCode}
import models.fileDetails._
import models.submission.SubmissionDetails
import models.{CBC401, ConversationId, MessageSpecData, NewInformation, TestData, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SendYourFileView

import scala.concurrent.{ExecutionContext, Future}

class SendYourFileControllerSpec extends SpecBase with Generators with ScalaCheckDrivenPropertyChecks {

  private val conversationId: ConversationId = ConversationId("conversationId")

  private val submissionDetailsArgCaptor: ArgumentCaptor[SubmissionDetails] = ArgumentCaptor.forClass(classOf[SubmissionDetails])

  val userAnswersWithContactDetails: UserAnswers = emptyUserAnswers
    .withPage(ContactNamePage, "test")
    .withPage(ContactEmailPage, "test@test.com")
    .withPage(HaveTelephonePage, true)
    .withPage(ContactPhonePage, "6677889922")
    .withPage(HaveSecondContactPage, true)
    .withPage(SecondContactNamePage, "test user")
    .withPage(SecondContactEmailPage, "t2@test.com")
    .withPage(SecondContactHavePhonePage, true)
    .withPage(SecondContactPhonePage, "8889988728")

  "SendYourFile Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET with a new report submission" in {
        forAll { (submissionDetails: SubmissionDetails) =>
          val fileData = ValidatedFileData(
            submissionDetails.fileName,
            submissionDetails.messageSpecData.copy(reportType = NewInformation),
            submissionDetails.fileSize,
            submissionDetails.checksum
          )

          val userAnswers = userAnswersWithContactDetails.withPage(ValidXMLPage, fileData)

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request   = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)
            val appConfig = application.injector.instanceOf[FrontendAppConfig]

            val result = route(application, request).value

            val view = application.injector.instanceOf[SendYourFileView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(appConfig, None)(request, messages(application)).toString
          }
        }
      }

      "redirect to 'file problem some information missing' page when userAnswers missing" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithContactDetails)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.FileProblemSomeInformationMissingController.onPageLoad().url
        }

      }

      "redirect to 'Some information missing' page when userAnswers missing contact details" in {

        val messageSpecData   = MessageSpecData("XBG1999999", CBC401, TestData, startDate, endDate, "Reporting Entity")
        val validatedFileData = ValidatedFileData("afile", messageSpecData, 20L, "MD5:123")

        val userAnswers = emptyUserAnswers.withPage(ValidXMLPage, validatedFileData)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SomeInformationMissingController.onPageLoad().url
        }

      }
    }

    "onSubmit" - {

      "redirect to still-checking-your-file page on successful submission" in {
        forAll { (submissionDetails: SubmissionDetails) =>
          val mockSubmissionConnector = mock[SubmissionConnector]

          val fileData = ValidatedFileData(
            submissionDetails.fileName,
            submissionDetails.messageSpecData,
            submissionDetails.fileSize,
            submissionDetails.checksum
          )

          val userAnswers = UserAnswers("Id")
            .withPage(ValidXMLPage, fileData)
            .withPage(URLPage, submissionDetails.documentUrl)
            .withPage(UploadIDPage, submissionDetails.uploadId)
            .withPage(FileReferencePage, submissionDetails.fileReference)

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[SubmissionConnector].toInstance(mockSubmissionConnector))
            .build()

          when(mockSubmissionConnector.submitDocument(submissionDetailsArgCaptor.capture())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(ConversationId("conversationId"))))

          running(application) {
            val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.FilePendingChecksController.onPageLoad().url)

            verifySubmissionDetails(submissionDetails)
          }
        }
      }

      "return INTERNAL_SERVER_ERROR on failing to submit document" in {
        forAll { (submissionDetails: SubmissionDetails) =>
          val mockSubmissionConnector = mock[SubmissionConnector]

          val fileData = ValidatedFileData(
            submissionDetails.fileName,
            submissionDetails.messageSpecData,
            submissionDetails.fileSize,
            submissionDetails.checksum
          )
          val userAnswers = UserAnswers("Id")
            .withPage(ValidXMLPage, fileData)
            .withPage(URLPage, submissionDetails.documentUrl)
            .withPage(UploadIDPage, submissionDetails.uploadId)
            .withPage(FileReferencePage, submissionDetails.fileReference)

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[SubmissionConnector].toInstance(mockSubmissionConnector))
            .build()

          when(mockSubmissionConnector.submitDocument(submissionDetailsArgCaptor.capture())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))

          running(application) {
            val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

            val result = route(application, request).value

            status(result) mustEqual INTERNAL_SERVER_ERROR

            verifySubmissionDetails(submissionDetails)
          }
        }
      }
    }

    "getStatus" - {

      "must return OK and load the page 'FileReceived' when the file status is 'Accepted'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(Accepted)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return NoContent when the file status is 'Pending'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(Pending)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual NO_CONTENT
        }
      }

      "must return OK and load the page 'FileRejected' when the file status is 'Rejected'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(Rejected(FileValidationErrors(None, None)))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must return OK and load the page 'ThereIsAProblem' when the file status is 'RejectedSDES'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(RejectedSDES)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(routes.ThereIsAProblemController.onPageLoad().url)
        }
      }

      "must return OK and load the page 'FileProblemVirus' when the file status is 'RejectedSDESVirus'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(RejectedSDESVirus)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(routes.FileProblemVirusController.onPageLoad().url)
        }
      }

      "must return OK and load the page 'ThereIsAProblemView' when the file status is 'Rejected' with expected business rules errors" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]
        val validationErrors = FileValidationErrors(Some(Seq(FileErrors(InvalidMessageRefIDFormat, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(Rejected(validationErrors))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(routes.FileRejectedController.onPageLoad(conversationId).url)
        }
      }

      "must return OK and load the page 'problem page' when the file status is 'Rejected' with 'problem' errors" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]
        val validationErrors = FileValidationErrors(Some(Seq(FileErrors(UnknownErrorCode("unknown-error"), None))),
                                                    Some(Seq(RecordError(UnknownErrorCode("unknown-error"), None, None)))
        )

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(Rejected(validationErrors))))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(routes.FileProblemController.onPageLoad().url)
        }
      }

      "must return OK and load the page 'Technical difficulties' page when getStatus returns no status" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .withPage(ConversationIdPage, conversationId)

        when(mockFileDetailsConnector.getStatus(any[ConversationId]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileDetailsConnector].toInstance(mockFileDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "must return OK and load the page 'Technical difficulties' page when ConversationId is None" in {

        val userAnswers = UserAnswers("Id")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.SendYourFileController.getStatus().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  private def verifySubmissionDetails(submissionDetails: SubmissionDetails) = {
    val capturedSubmissionDetails = submissionDetailsArgCaptor.getValue
    capturedSubmissionDetails.uploadId mustBe submissionDetails.uploadId
    capturedSubmissionDetails.documentUrl mustBe submissionDetails.documentUrl
    capturedSubmissionDetails.fileName mustBe submissionDetails.fileName
    capturedSubmissionDetails.messageSpecData mustBe submissionDetails.messageSpecData
    capturedSubmissionDetails.fileSize mustBe submissionDetails.fileSize
    capturedSubmissionDetails.checksum mustBe submissionDetails.checksum
  }
}

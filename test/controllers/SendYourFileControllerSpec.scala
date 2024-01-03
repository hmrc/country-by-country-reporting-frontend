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
import handlers.XmlHandler
import models.fileDetails.FileErrorCode.FailedSchemaValidation
import models.fileDetails.RecordErrorCode.DocRefIDFormat
import models.fileDetails._
import models.{CBC401, CBC402, ConversationId, MessageSpecData, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentMatchers.any
import pages.{ConversationIdPage, URLPage, ValidXMLPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SendYourFileView

import scala.concurrent.{ExecutionContext, Future}

class SendYourFileControllerSpec extends SpecBase {

  private val conversationId: ConversationId = ConversationId("conversationId")

  "SendYourFile Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view with no warning text for a GET" in {

        val userAnswers = UserAnswers("Id")
          .set(ValidXMLPage, ValidatedFileData("fileName", MessageSpecData("messageRef", CBC401, "Reporting Entity")))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request   = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(displayWarning = false, appConfig)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view with some warning text for a GET" in {

        val userAnswers = UserAnswers("Id")
          .set(ValidXMLPage, ValidatedFileData("fileName", MessageSpecData("messageRef", CBC402, "Reporting Entity")))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request   = FakeRequest(GET, routes.SendYourFileController.onPageLoad().url)
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val result = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(displayWarning = true, appConfig)(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {

      "redirect to file received page" in {

        val mockSubmissionConnector = mock[SubmissionConnector]
        val mockXmlHandler          = mock[XmlHandler]

        val userAnswers = UserAnswers("Id")
          .set(ValidXMLPage, ValidatedFileData("fileName", MessageSpecData("messageRef", CBC402, "Reporting Entity")))
          .success
          .value
          .set(URLPage, "url")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubmissionConnector].toInstance(mockSubmissionConnector),
            bind[XmlHandler].toInstance(mockXmlHandler)
          )
          .build()

        when(mockSubmissionConnector.submitDocument(any[String], any[String], any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(ConversationId("conversationId"))))

        when(mockXmlHandler.load(any[String]())).thenReturn(<test><value>Success</value></test>)

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.FilePendingChecksController.onPageLoad().url)

          verify(mockSubmissionConnector, times(1))
            .submitDocument(any(), any(), any())(any(), any())
        }
      }

      "redirect to there is a problem page if userAnswers missing" in {

        val userAnswers = UserAnswers("Id")
          .set(ValidXMLPage, ValidatedFileData("fileName", MessageSpecData("messageRef", CBC402, "Reporting Entity")))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "redirect to there is a problem page on failing to submitDocument" in {
        val mockSubmissionConnector = mock[SubmissionConnector]
        val mockXmlHandler          = mock[XmlHandler]

        val userAnswers = UserAnswers("Id")
          .set(ValidXMLPage, ValidatedFileData("fileName", MessageSpecData("messageRef", CBC402, "Reporting Entity")))
          .success
          .value
          .set(URLPage, "url")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SubmissionConnector].toInstance(mockSubmissionConnector),
            bind[XmlHandler].toInstance(mockXmlHandler)
          )
          .build()

        when(mockXmlHandler.load(any[String]())).thenReturn(<test><value>Success</value></test>)

        when(mockSubmissionConnector.submitDocument(any[String], any[String], any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        running(application) {
          val request = FakeRequest(POST, routes.SendYourFileController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }

    "getStatus" - {

      "must return OK and load the page 'FileReceived' when the file status is 'Accepted'" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .set(ConversationIdPage, conversationId)
          .success
          .value

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
          .set(ConversationIdPage, conversationId)
          .success
          .value

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
          .set(ConversationIdPage, conversationId)
          .success
          .value

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

      "must return OK and load the page 'FileProblem' when the file status is 'Rejected' with 'problem' errors" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]
        val validationErrors         = FileValidationErrors(Some(Seq(FileErrors(FailedSchemaValidation, None))), Some(Seq(RecordError(DocRefIDFormat, None, None))))

        val userAnswers = UserAnswers("Id")
          .set(ConversationIdPage, conversationId)
          .success
          .value

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
        }
      }

      "must return OK and load the page 'Technical difficulties' page when getStatus returns no status" in {

        val mockFileDetailsConnector = mock[FileDetailsConnector]

        val userAnswers = UserAnswers("Id")
          .set(ConversationIdPage, conversationId)
          .success
          .value

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
}

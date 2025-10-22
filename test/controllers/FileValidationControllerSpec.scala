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
import connectors.{UpscanConnector, ValidationConnector}
import helpers.FakeUpscanConnector
import models.upscan.{Reference, UploadId, UploadSessionDetails, UploadedSuccessfully}
import models.{CBC401, GenericError, InvalidXmlError, Message, MessageSpecData, TestData, UserAnswers, ValidatedFileData, ValidationErrors}
import org.bson.types.ObjectId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import pages.{FileReferencePage, UploadIDPage}
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ThereIsAProblemView

import scala.concurrent.{ExecutionContextExecutor, Future}

class FileValidationControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockValidationConnector: ValidationConnector = mock[ValidationConnector]

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  override def beforeEach: Unit =
    reset(mockSessionRepository)

  val fakeUpscanConnector: FakeUpscanConnector = app.injector.instanceOf[FakeUpscanConnector]

  "FileValidationController" - {
    val uploadId        = UploadId("123")
    val fileReferenceId = Reference("fileReferenceId")
    val userAnswers     = UserAnswers(userAnswersId).withPage(UploadIDPage, uploadId).withPage(FileReferencePage, fileReferenceId)
    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[UpscanConnector].toInstance(fakeUpscanConnector),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[ValidationConnector].toInstance(mockValidationConnector)
      )
      .build()

    val downloadURL = "http://dummy-url.com"
    val FileSize    = 20L

    val uploadDetails = UploadSessionDetails(
      new ObjectId(),
      uploadId,
      Reference("123"),
      UploadedSuccessfully("afile", downloadURL, FileSize, "MD5:123")
    )

    "must redirect to Check your answers and present the correct view for a GET" in {

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val messageSpecData                                = MessageSpecData("XBG1999999", CBC401, TestData, startDate, endDate, "Reporting Entity")
      val expectedData: JsObject = Json.obj(
        "uploadID"      -> uploadId,
        "FileReference" -> fileReferenceId,
        "validXML"      -> ValidatedFileData("afile", messageSpecData, FileSize, "MD5:123"),
        "url"           -> downloadURL
      )

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Right(messageSpecData)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.CheckYourFileDetailsController.onPageLoad().url

      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustEqual expectedData
    }

    "must redirect to Upload File error page when file name length is more than 100 characters" in {
      val uploadDetails = UploadSessionDetails(
        new ObjectId(),
        uploadId,
        Reference("123"),
        UploadedSuccessfully("FileNameMoreThan100ChFileNameMoreThan100ChFileNameMoreThan100ChFileNameMoreThan100ChFileNameMoreThan1.xml",
                             downloadURL,
                             FileSize,
                             "MD5:123"
        )
      )

      fakeUpscanConnector.setDetails(uploadDetails)

      val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
      val result: Future[Result] = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual routes.UploadFileController.showError("InvalidArgument", "InvalidFileNameLength", "123").url
    }

    "must redirect to Upload File error page when file name contains disallowed characters" in {
      val disallowedCharacters: Set[Char] = Set('<', '>', ':', '"', '/', '\\', '|', '?', '*')
      disallowedCharacters.foreach {
        ch =>
          val uploadDetails = UploadSessionDetails(
            new ObjectId(),
            uploadId,
            Reference("123"),
            UploadedSuccessfully(s"filenamecontains$ch.xml", downloadURL, FileSize, "MD5:123")
          )

          fakeUpscanConnector.setDetails(uploadDetails)

          val request                = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)
          val result: Future[Result] = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual routes.UploadFileController.showError("InvalidArgument", "DisallowedCharacters", "123").url
      }

    }

    "must redirect to invalid XML page if XML validation fails" in {

      val errors: Seq[GenericError]                      = Seq(GenericError(1, Message("error")))
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val expectedData                                   = Json.obj("invalidXML" -> "afile", "errors" -> errors, "uploadID" -> uploadId, "FileReference" -> fileReferenceId)

      fakeUpscanConnector.setDetails(uploadDetails)

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(ValidationErrors(errors, None))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.data mustEqual expectedData
    }

    "must redirect to file error page if XML parser fails" in {

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val expectedData                                   = Json.obj("invalidXML" -> "afile", "uploadID" -> UploadId("123"), "FileReference" -> fileReferenceId)

      fakeUpscanConnector.setDetails(uploadDetails)
      //noinspection ScalaStyle

      when(mockValidationConnector.sendForValidation(any())(any(), any())).thenReturn(Future.successful(Left(InvalidXmlError("sax exception"))))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val controller             = application.injector.instanceOf[FileValidationController]
      val result: Future[Result] = controller.onPageLoad()(FakeRequest("", ""))

      status(result) mustBe SEE_OTHER
      verify(mockSessionRepository, times(1)).set(userAnswersCaptor.capture())
      redirectLocation(result) mustBe Some(routes.FileErrorController.onPageLoad().url)
      userAnswersCaptor.getValue.data mustEqual expectedData
    }

    "must return ThereIsAProblemPage when a valid UploadId cannot be found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[ValidationConnector].toInstance(mockValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must return ThereIsAProblemPage when meta data cannot be found" in {

      fakeUpscanConnector.resetDetails()

      running(application) {
        val request = FakeRequest(GET, routes.FileValidationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}

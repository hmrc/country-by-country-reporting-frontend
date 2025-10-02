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
import connectors.UpscanConnector
import controllers.actions._
import forms.UploadFileFormProvider
import generators.Generators
import helpers.FakeUpscanConnector
import models.upscan._
import models.{CBC401, MessageSpecData, TestData, UserAnswers, ValidatedFileData}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import repositories.SessionRepository
import services.{AgentSubscriptionService, SubscriptionService}
import views.html.UploadFileView

import scala.concurrent.Future

class UploadFileControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val FileSize   = 20L
  val uploadId: UploadId = UploadId("12345")

  val fakeUpscanConnector: FakeUpscanConnector = app.injector.instanceOf[FakeUpscanConnector]

  val userAnswers = emptyUserAnswers
    .withPage(UploadIDPage, UploadId("uploadId"))
    .withPage(ContactNamePage, "test")
    .withPage(ContactEmailPage, "test@test.com")
    .withPage(HaveTelephonePage, true)
    .withPage(ContactPhonePage, "6677889922")
    .withPage(HaveSecondContactPage, true)
    .withPage(SecondContactNamePage, "test user")
    .withPage(SecondContactEmailPage, "t2@test.com")
    .withPage(SecondContactHavePhonePage, true)
    .withPage(SecondContactPhonePage, "8889988728")

  "upload file controller" - {

    "must redirect to some information missing page when missing contact information for organisation " in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val userAnswers = emptyUserAnswers
        .withPage(UploadIDPage, UploadId("uploadId"))

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/send-a-country-by-country-report/problem/some-information-is-missing")
    }

    "must redirect to some information missing page when missing contact information for agent " in {
      val mockSubscriptionService      = mock[SubscriptionService]
      val mockAgentSubscriptionService = mock[AgentSubscriptionService]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val userAnswers = emptyUserAnswers
        .withPage(UploadIDPage, UploadId("uploadId"))
        .withPage(AgentFirstContactNamePage, "AgentName")
        .withPage(AgentClientIdPage, "subscriptionId")

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(userAnswers))),
          bind[UpscanConnector].toInstance(fakeUpscanConnector),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[AgentSubscriptionService].toInstance(mockAgentSubscriptionService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[IdentifierAction].to[FakeIdentifierActionAgent]
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/send-a-country-by-country-report/agent/problem/some-information-is-missing")
    }

    "must initiate a request to upscan to bring back an upload form" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val form    = app.injector.instanceOf[UploadFileFormProvider]
      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[UploadFileView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form(), UpscanInitiateResponse(Reference(""), "target", Map.empty))(request, messages(application)).toString
    }

    "onPageLoad must remove any validXml page in the session" in {
      val messageSpecData   = MessageSpecData("XBG1999999", CBC401, TestData, startDate, endDate, "Reporting Entity")
      val validatedFileData = ValidatedFileData("afile", messageSpecData, FileSize, "MD5:123")

      val userAnswersWithValidXmlPage: UserAnswers = userAnswers.set(ValidXMLPage, validatedFileData).success.value

      val application: Application = applicationBuilder(userAnswers = Some(userAnswersWithValidXmlPage))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val form    = app.injector.instanceOf[UploadFileFormProvider]
      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[UploadFileView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form(), UpscanInitiateResponse(Reference(""), "target", Map.empty))(request, messages(application)).toString

      verify(mockSessionRepository).set(argThat {
        answers: UserAnswers =>
          answers.get(FileReferencePage).isDefined &&
          answers.get(UploadIDPage).isDefined &&
          answers.get(ValidXMLPage).isEmpty
      })
    }

    "must read the progress of the upload from the backend" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.getStatus(uploadId).url)

      def verifyResult(uploadStatus: UploadStatus, expectedResult: Option[String] = None): Unit = {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UpscanConnector].toInstance(fakeUpscanConnector)
          )
          .build()

        fakeUpscanConnector.setStatus(uploadStatus)
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe expectedResult
        application.stop()
      }

      verifyResult(InProgress, Some(routes.UploadFileController.getStatus(uploadId).url))
      verifyResult(Quarantined, Some(routes.FileProblemVirusController.onPageLoad().url))
      verifyResult(UploadRejected(ErrorDetails("REJECTED", "message")), Some(routes.FileProblemNotXmlController.onPageLoad().url))
      verifyResult(Failed, Some(routes.ThereIsAProblemController.onPageLoad().url))
      verifyResult(UploadedSuccessfully("name", "downloadUrl", FileSize, "MD5:123"), Some(routes.FileValidationController.onPageLoad().url))

    }

    "must show any returned error" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.showError("errorCode", "errorMessage", "errorReqId").url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
    }

    "must show returned error when file name length is invalid" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.showError("InvalidArgument", "InvalidFileNameLength", "errorReqId").url)
      val result  = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) contains "File name must be 100 characters or less and match the MessageRefId in the file"
    }

    "must show returned error with an InvalidArgument and include 'Error:' in the title" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.UploadFileController.showError("InvalidArgument", "errorMessage", "errorReqId").url)
      val result  = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK
      content must include("Select a file to upload")
      content must include("<title>Error:")
    }

    "must show File to large error when the errorCode is EntityTooLarge" in {

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UpscanConnector].toInstance(fakeUpscanConnector)
        )
        .build()

      val request =
        FakeRequest(GET, routes.UploadFileController.showError("EntityTooLarge", "Your proposed upload exceeds the maximum allowed size", "errorReqId").url)
      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.FileProblemTooLargeController.onPageLoad().url)
    }

  }
}

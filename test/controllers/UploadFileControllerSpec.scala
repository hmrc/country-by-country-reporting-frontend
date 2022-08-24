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
import connectors.UpscanConnector
import forms.UploadFileFormProvider
import generators.Generators
import helpers.FakeUpscanConnector
import models.UserAnswers
import models.upscan._
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.UploadIDPage
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import views.html.UploadFileView

import scala.concurrent.Future

class UploadFileControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val fakeUpscanConnector: FakeUpscanConnector = app.injector.instanceOf[FakeUpscanConnector]

  val userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(UploadIDPage, UploadId("uploadId"))
    .success
    .value

  val application: Application = applicationBuilder(userAnswers = Some(userAnswers))
    .overrides(
      bind[UpscanConnector].toInstance(fakeUpscanConnector)
    )
    .build()

  "upload file controller" - {

    "must initiate a request to upscan to bring back an upload form" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val form    = app.injector.instanceOf[UploadFileFormProvider]
      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[UploadFileView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form(), UpscanInitiateResponse(Reference(""), "target", Map.empty))(request, messages(application)).toString
    }

    "must read the progress of the upload from the backend" in {

      val request = FakeRequest(GET, routes.UploadFileController.getStatus().url)

      def verifyResult(uploadStatus: UploadStatus, expectedStatus: Int = OK, expectedResult: Option[URL] = None): Unit = {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UpscanConnector].toInstance(fakeUpscanConnector)
          )
          .build()

        fakeUpscanConnector.setStatus(uploadStatus)
        val result = route(application, request).value

        status(result) mustBe expectedStatus
        val actualResult = Option(contentAsString(result)).collect { case x if x.trim.nonEmpty => x }.map(Json.parse(_).as[URL])
        actualResult mustBe expectedResult
        application.stop()
      }

      verifyResult(InProgress, CONTINUE, None)
      verifyResult(Quarantined, OK, Some(URL("/send-a-country-by-country-report/problem/virus-found")))
      verifyResult(
        UploadRejected(ErrorDetails("REJECTED", "message")),
        OK,
        Some(URL("/send-a-country-by-country-report/problem/not-xml"))
      )
      verifyResult(Failed, OK, Some(URL("/send-a-country-by-country-report/problem/there-is-a-problem")))
      verifyResult(UploadedSuccessfully("name", "downloadUrl"), OK, Some(URL("/send-a-country-by-country-report/file-validation")))

    }

    "must show any returned error" in {

      val request = FakeRequest(GET, routes.UploadFileController.showError("errorCode", "errorMessage", "errorReqId").url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
    }

    "must show File to large error when the errorCode is EntityTooLarge" in {

      val request =
        FakeRequest(GET, routes.UploadFileController.showError("EntityTooLarge", "Your proposed upload exceeds the maximum allowed size", "errorReqId").url)
      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.FileProblemTooLargeController.onPageLoad().url)
    }

  }
}

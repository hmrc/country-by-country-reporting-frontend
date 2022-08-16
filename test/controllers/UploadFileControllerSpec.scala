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
import helpers.FakeUpscanConnector
import models.UserAnswers
import models.upscan.{Reference, UploadId, UpscanInitiateResponse}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadIDPage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UploadFileView

import scala.concurrent.Future

class UploadFileControllerSpec extends SpecBase with MockitoSugar {

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

  "UploadFile Controller" - {

    "must initiate a request to upscan to bring back an upload form" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val form    = app.injector.instanceOf[UploadFileFormProvider]
      val request = FakeRequest(GET, routes.UploadFileController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[UploadFileView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form(), UpscanInitiateResponse(Reference(""), "target", Map.empty))(request, messages(application)).toString
    }
  }
}

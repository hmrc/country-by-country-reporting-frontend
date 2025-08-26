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
import controllers.actions._
import models.{CBC401, MessageSpecData, TestData, UserAnswers, ValidatedFileData}
import pages.ValidXMLPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.CheckYourFileDetailsViewModel
import viewmodels.govuk.summarylist._
import views.html.{CheckYourFileDetailsView, SomeInformationMissingView}

class CheckYourFileDetailsControllerSpec extends SpecBase {

  private val FileSize = 20L

  "CheckYourFileDetails Controller" - {
    val vfd: ValidatedFileData = ValidatedFileData(
      "filename.xml",
      MessageSpecData("messageRefId", CBC401, "Reporting Entity", TestData),
      FileSize,
      "MD5:123"
    )

    "must return OK and the correct view for a GET" in {

      val ua: UserAnswers = emptyUserAnswers.set(ValidXMLPage, vfd).success.value
      val application     = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourFileDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourFileDetailsView]

        val list = SummaryListViewModel(CheckYourFileDetailsViewModel.getSummaryRows(vfd)(messages(application)))
          .withoutBorders()
          .withCssClass("govuk-!-margin-bottom-0")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for Agent" in {

      val ua: UserAnswers = emptyUserAnswers.set(ValidXMLPage, vfd).success.value
      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeIdentifierActionAgent],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(ua)))
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourFileDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourFileDetailsView]

        val list = SummaryListViewModel(CheckYourFileDetailsViewModel.getAgentSummaryRows(vfd)(messages(application)))
          .withoutBorders()
          .withCssClass("govuk-!-margin-bottom-0")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must return internal server error when there is no ValidXMLPage in request" in {
      val uploadUrlPath   = routes.UploadFileController.onPageLoad().url
      val ua: UserAnswers = emptyUserAnswers

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeIdentifierActionAgent],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionProvider(Some(ua)))
        )
        .build()

      val request = FakeRequest(GET, routes.CheckYourFileDetailsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[SomeInformationMissingView]

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.FileProblemSomeInformationMissingController.onPageLoad().url
    }
  }
}

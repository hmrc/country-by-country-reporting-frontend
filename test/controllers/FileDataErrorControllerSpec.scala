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
import models.{GenericError, Message}
import pages.{GenericErrorPage, InvalidXMLPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import views.html.FileDataErrorView

class FileDataErrorControllerSpec extends SpecBase {

  private val fileName = "fileName"
  private val error    = "Some Error"

  private val errorRows = Seq(
    Seq(
      TableRow(content = Text("1"), classes = "govuk-table__cell--numeric", attributes = Map("id" -> "lineNumber_1")),
      TableRow(content = Text(error), attributes = Map("id" -> "errorMessage_1"))
    )
  )

  "FileDataError Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(GenericErrorPage, Seq(GenericError(1, Message(error))))
        .success
        .value
        .set(InvalidXMLPage, fileName)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileDataErrorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileDataErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileName, errorRows)(request, messages(application)).toString
      }
    }

    "must return Internal server error on failing to read error details from userAnswers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileDataErrorController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}

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
import pages.InvalidXMLPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{FileErrorView, ThereIsAProblemView}

class FileErrorControllerSpec extends SpecBase {

  "FileError Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(InvalidXMLPage, "example.xml").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileErrorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileErrorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view("example.xml")(request, messages(application)).toString
      }
    }

    "must return INTERNAL_SERVER_ERROR and the correct view when InvalidXMLPage does not exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileErrorController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThereIsAProblemView]

        status(result) mustEqual INTERNAL_SERVER_ERROR

        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}

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
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SomeInformationMissingControllerSpec extends SpecBase with BeforeAndAfterEach {

  "SomeInformationMissing Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET and show 'continue' button" in {
        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "tester")
          .withPage(ContactEmailPage, "tester@test.com")
          .withPage(HaveTelephonePage, true)
          .withPage(ContactPhonePage, "7778889993")
          .withPage(HaveSecondContactPage, false)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SomeInformationMissingController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementsContainingText("Continue").isEmpty mustBe false
          doc.getElementsContainingText("Some information is missing").isEmpty mustBe false
        }
      }
    }
  }
}

/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.SpecBase
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import utils.ViewHelper
import views.html.FileProblemTooLargeView

class FileProblemTooLargeSpec extends SpecBase with Injecting with ViewHelper {

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  private def setupDoc = {
    val view: FileProblemTooLargeView                                     = inject[FileProblemTooLargeView]
    val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
    implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))
    Jsoup.parse(view().body)
  }

  "FileProblemTooLargeView" - {
    "should have guidance link" in {
      val elem = setupDoc.getElementById("guidance_link")
      elem.attr("href") mustEqual "https://www.gov.uk/guidance/send-a-country-by-country-report#how-to-create-your-report"
    }
  }

}

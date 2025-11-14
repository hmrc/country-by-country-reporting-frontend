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
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.IndexView

class IndexViewSpec extends SpecBase with Injecting with ViewHelper {

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "IndexView" - {
    "should render page components" in {

      val view1: IndexView                                                  = inject[IndexView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val renderedHtml: HtmlFormat.Appendable =
        view1(false, "XTCBC0100000001", false)
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Manage your country-by-country report")
      getPageHeading(doc) mustEqual "Manage your country-by-country report"
      val paragraphValues = getAllParagraph(doc).text()
      paragraphValues must include("You can report new information, or corrections and deletions, by uploading an XML file.")
      paragraphValues must include("The file must include your CBC ID XTCBC0100000001 and be 100MB or less.")
      paragraphValues must include("You can also change your contact details.")
      elementText(doc, "#submit") mustEqual "Upload an XML file"
      paragraphValues must include(
        "Email your HMRC Customer Compliance Manager or msb.countrybycountryreportingmailbox@hmrc.gov.uk if you are having problems with the service."
      )
      val linkElements = doc.select(".govuk-link")
      linkElements.select(":contains(change your contact details)").attr("href") mustEqual "/send-a-country-by-country-report/change-contact/details"
    }
  }

}

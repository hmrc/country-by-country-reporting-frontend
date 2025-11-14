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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import views.html.UnauthorisedView

class UnauthorisedViewSpec extends SpecBase with Injecting with ViewHelper {

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "UnauthorisedView" - {
    "should render page components" in {
      val view1: UnauthorisedView                                           = inject[UnauthorisedView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val renderedHtml: HtmlFormat.Appendable =
        view1()
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("You cannot access this page")
      getPageHeading(doc) mustEqual "You cannot access this page"
      getAllParagraph(doc).text() must include(
        "You can email your HMRC Customer Compliance Manager or msb.countrybycountryreportingmailbox@hmrc.gov.uk if you need support with using the service."
      )
      val linkElements = doc.select(".govuk-link")
      linkElements.select(":contains(Refer to the country-by-country reporting guidance)").attr("href") mustEqual "#"
    }
  }

}

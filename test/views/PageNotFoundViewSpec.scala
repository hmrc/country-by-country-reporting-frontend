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
import views.html.PageNotFoundView

class PageNotFoundViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view1: PageNotFoundView                                           = app.injector.instanceOf[PageNotFoundView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "PageNotFoundView" - {
    "should render page components" in {
      val renderedHtml: HtmlFormat.Appendable =
        view1()
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Page not found")
      getPageHeading(doc) mustEqual "Page not found"
      val paragraphValues = getAllParagraph(doc).text()
      paragraphValues must include("If you typed the web address, check it is correct.")
      paragraphValues must include("If you pasted the web address, check you copied the entire address.")
      paragraphValues must include(
        "You can email your HMRC Customer Compliance Manager or msb.countrybycountryreportingmailbox@hmrc.gov.uk if you have any questions about this service."
      )
    }
  }

}

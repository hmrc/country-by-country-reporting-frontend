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
import views.html.FileProblemView

class FileProblemViewSpec extends SpecBase with Injecting with ViewHelper {

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "FileProblemView" - {
    "should render page components" in {
      val view1: FileProblemView                                            = inject[FileProblemView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val renderedHtml: HtmlFormat.Appendable =
        view1()
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("Sorry, there is a problem with the service")
      getPageHeading(doc) mustEqual "Sorry, there is a problem with the service"
      val paragraphValues = getAllParagraph(doc).text()
      paragraphValues must include("We have not accepted your file.")
      paragraphValues must include(
        "You must email your HMRC Customer Compliance Manager or msb.countrybycountryreportingmailbox@hmrc.gov.uk so we can resolve the problem. Include the subject line ‘CBC file not accepted’."
      )
    }
  }

}

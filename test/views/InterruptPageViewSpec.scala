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
import views.html.InterruptPageView

class InterruptPageViewSpec extends SpecBase with Injecting with ViewHelper {

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "InteruptPageView" - {
    "should render page components" in {
      val view1: InterruptPageView                                          = inject[InterruptPageView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val renderedHtml: HtmlFormat.Appendable =
        view1()
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("The service is temporarily unavailable")
      val pageHeadings = getPageHeading(doc)
      pageHeadings must include("The service is temporarily unavailable")
      pageHeadings must include("If you need to send a report soon")
      pageHeadings must include("Arranging an online session")
      val paragraphValues = getAllParagraph(doc).text()
      paragraphValues must include("We’re making some changes to the country-by-country reporting service.")
      paragraphValues must include("If your report is due soon, then you’ll need to arrange an online session with us to use the service.")
      paragraphValues must include("To avoid potential penalties, please contact us before your usual reporting deadline.")
      paragraphValues must include(
        "For a limited time, you’ll only be able to access the service through an online session with us. We’ll let you know when the service is live again."
      )
      paragraphValues must include(
        "These sessions are optional, unless you need to send a report soon. We’ll be offering support in the sessions, as well as asking for feedback."
      )
      paragraphValues must include("To arrange a session, email cbcdigital@digital.hmrc.gov.uk with a preferred date and time.")
      paragraphValues must include("If you’re an agent, you must do the following before your session:")
      elementText(doc, "li") must include("create an agent services account, if you do not have one already")
      elementText(doc, "li") must include("ask your client to authorise you for country-by-country reporting")
      elementText(doc, "a") must include("ask your client to authorise you")
      elementText(doc, "a") must include("create an agent services account")
    }
  }

}

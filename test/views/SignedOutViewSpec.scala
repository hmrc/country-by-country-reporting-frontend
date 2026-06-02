/*
 * Copyright 2026 HM Revenue & Customs
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
import org.jsoup.Jsoup.parse
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import utils.ViewHelper
import views.html.auth.SignedOutView

class SignedOutViewSpec extends SpecBase with Injecting with ViewHelper {
  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "SignedOutView" - {
    "should render page components" in {

      val view1: SignedOutView                                              = inject[SignedOutView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val renderedHtml = view1()
      lazy val doc     = parse(renderedHtml.body)

      getWindowTitle(doc) must include("For your security, we signed you out")
      getPageHeading(doc) mustEqual "For your security, we signed you out"

      val signInLink = doc.select("a.govuk-link[href]").stream().filter(_.text() == "Sign in again to use the service").findFirst().get()
      signInLink.text() mustEqual "Sign in again to use the service"
      signInLink.attr("href") mustEqual "http://localhost:9949/auth-login-stub/gg-sign-in"
    }
  }

}

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
import org.scalatest.matchers.must.Matchers.include
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.i18n.{Lang, Messages}
import play.api.test.{FakeRequest, Injecting}
import play.test.Helpers.fakeRequest
import utils.ViewHelper
import views.html.{FileProblemView, ProblemClientAccessView}

class ProblemClientAccessViewSpec extends SpecBase with GuiceOneAppPerSuite with Injecting with ViewHelper {

  val view: ProblemClientAccessView                                     = app.injector.instanceOf[ProblemClientAccessView]
  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

  "ProblemClientAccessView" - {
    "should render page components" in {

      val renderedHtml = view()
      lazy val doc     = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) mustEqual "You must have access from your organisation to report for this client - Send a country-by-country report - GOV.UK"
      getPageHeading(doc) mustEqual "You must have access from your organisation to report for this client"

      val paragraphValues = getAllParagraph(doc).text()
      paragraphValues must include("This client has authorised your organisation to report for them, but you do not have access.")
      paragraphValues must include(
        "An administrator for your agent services account can manage access through access groups. This is under ‘Manage account’ in agent services."
      )
      paragraphValues must include(
        "If you’re not an administrator, you can ask an administrator to give you access. You can find a list of your administrators in your agent services account."
      )

      val linkElements = doc.select(".govuk-link")
      linkElements
        .select(":contains(Go to your agent services account)")
        .attr("href") mustEqual "https://www.tax.service.gov.uk/agent-services-account/no-assignment"
    }
  }

}

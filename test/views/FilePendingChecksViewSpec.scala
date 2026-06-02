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
import controllers.routes
import models.fileDetails.Pending
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.HtmlFormat
import utils.ViewHelper
import viewmodels.FileCheckViewModel
import views.html.FilePendingChecksView

class FilePendingChecksViewSpec extends SpecBase with Injecting with ViewHelper {
  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  "FilePendingChecksView" - {
    "should render page components" in {
      val messageRefId                                                      = "testMessageRefId"
      val testView: FilePendingChecksView                                   = inject[FilePendingChecksView]
      val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
      implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))

      val summary = FileCheckViewModel.createFileSummary(messageRefId, Pending.toString)

      val paragraphs = Seq(
        "You need to refresh the page for updates on the status of our automatic checks.",
        "If you have been refreshing for more than 10 minutes, you can sign out."
      )

      val renderedHtml: HtmlFormat.Appendable =
        testView(summary, routes.FilePendingChecksController.onPageLoad().url, "conversatonId", "10", false)
      lazy val doc = Jsoup.parse(renderedHtml.body)

      getWindowTitle(doc) must include("We need a few more minutes to check your file")
      getPageHeading(doc) mustEqual "We need a few more minutes to check your file"

      doc.select(".govuk-summary-list__row").get(0).select(".govuk-summary-list__key").text() must include("File ID (MessageRefId)")
      doc.select(".govuk-summary-list__row").get(0).select(".govuk-summary-list__value").text() must include(
        "testMessageRefId"
      )
      doc.select(".govuk-summary-list__row").get(1).select(".govuk-summary-list__key").text() must include("Result of automatic checks")
      doc.select(".govuk-summary-list__row").get(1).select(".govuk-summary-list__value").text() must include("Pending")

      paragraphs.foreach(paragraph => getAllParagraph(doc).text() must include(paragraph))
    }
  }
}

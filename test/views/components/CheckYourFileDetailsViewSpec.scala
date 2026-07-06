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

package views.components

import base.SpecBase
import models.{CBC401, CorrectionForExistingReport, MessageSpecData, ValidatedFileData}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import utils.ViewHelper
import viewmodels.CheckYourFileDetailsViewModel
import viewmodels.govuk.all.SummaryListViewModel
import views.html.{CheckYourFileDetailsView, FileErrorView}

class CheckYourFileDetailsViewSpec extends SpecBase with Injecting with ViewHelper {
  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  private def setupDoc = {
    val validatedFileData: ValidatedFileData = ValidatedFileData(
      fileName = "testFi",
      fileSize = 1234,
      messageSpecData = MessageSpecData(
        messageRefId = "test'RefId",
        messageTypeIndic = CBC401,
        reportType = CorrectionForExistingReport,
        reportingPeriodStartDate = java.time.LocalDate.of(2024, 1, 1),
        reportingPeriodEndDate = java.time.LocalDate.of(2024, 12, 31),
        reportingEntityName = "Test Entity"
      ),
      checksum = "some-checksum"
    )
    val view: CheckYourFileDetailsView                                    = inject[CheckYourFileDetailsView]
    val messagesControllerComponentsForView: MessagesControllerComponents = inject[MessagesControllerComponents]
    implicit val messages: Messages                                       = messagesControllerComponentsForView.messagesApi.preferred(Seq(Lang("en")))
    val summaryList = SummaryListViewModel(CheckYourFileDetailsViewModel.getAgentSummaryRows(validatedFileData))

    Jsoup.parse(view(summaryList).body)
  }

  "CheckYourFileDetailsView" - {
    "should render page components" in {
      val doc = setupDoc

      getWindowTitle(doc) must include("Check your file details are correct")

      doc.select(".govuk-summary-list__row").get(0).select(".govuk-summary-list__key").text() must include("File ID (MessageRefId)")
      doc.select(".govuk-summary-list__row").get(0).select(".govuk-summary-list__value").text() must include(
        "test'RefId"
      )

      doc.select(".govuk-summary-list__row").get(1).select(".govuk-summary-list__key").text() must include("Client (ReportingEntity Name)")
      doc.select(".govuk-summary-list__row").get(1).select(".govuk-summary-list__value").text() must include(
        "Test Entity"
      )

      doc.select(".govuk-summary-list__row").get(2).select(".govuk-summary-list__key").text() must include("File information")
      doc.select(".govuk-summary-list__row").get(2).select(".govuk-summary-list__value").text() must include(
        "Corrections for an existing report"
      )

    }
  }
}

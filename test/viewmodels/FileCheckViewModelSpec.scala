/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import base.SpecBase
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

class FileCheckViewModelSpec extends SpecBase {

  "FileCheckViewModel" - {

    "must return the viewModel when fileStatus is pending" in {

      val expectedSummary = Seq(
        SummaryListRow(Key(Text("File name")), Value(Text("name1.xml")), "", None),
        SummaryListRow(Key(Text("Result of automatic checks")), Value(HtmlContent("<strong class='govuk-tag govuk-tag--yellow'>Pending</strong>")), "", None)
      )

      FileCheckViewModel.createFileSummary("name1.xml", "Pending")(messages(app)) mustBe expectedSummary
    }

    "must return the viewModel when fileStatus is passed" in {

      val expectedSummary = Seq(
        SummaryListRow(Key(Text("File name")), Value(Text("name1.xml")), "", None),
        SummaryListRow(Key(Text("Result of automatic checks")), Value(HtmlContent("<strong class='govuk-tag govuk-tag--green'>Passed</strong>")), "", None)
      )

      FileCheckViewModel.createFileSummary("name1.xml", "Accepted")(messages(app)) mustBe expectedSummary
    }

    "must return the viewModel when fileStatus is rejected" in {

      val expectedSummary = Seq(
        SummaryListRow(Key(Text("File name")), Value(Text("name1.xml")), "", None),
        SummaryListRow(Key(Text("Result of automatic checks")), Value(HtmlContent("<strong class='govuk-tag govuk-tag--red'>Failed</strong>")), "", None)
      )

      FileCheckViewModel.createFileSummary("name1.xml", "Rejected")(messages(app)) mustBe expectedSummary
    }
  }
}

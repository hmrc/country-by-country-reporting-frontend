/*
 * Copyright 2024 HM Revenue & Customs
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
import models.AgentClientDetails
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow}

class AgentClientDetailsViewModelSpec extends SpecBase {
  "AgentClientDetailsViewModel" - {

    "must return the viewModel containing tradingName" in {

      val expectedSummary = Seq(
        SummaryListRow(Key(Text("Client name")), Value(Text("Trading Name")), "", Some(Actions("", Seq.empty[ActionItem]))),
        SummaryListRow(
          Key(Text("Client CBC ID")),
          Value(Text("CBCXX1")),
          "",
          Some(
            Actions(
              "",
              Seq(
                ActionItem(
                  "/send-a-country-by-country-report/agent/manage-your-clients",
                  HtmlContent("""<span aria-hidden="true">Change</span><span class="govuk-visually-hidden">Change client</span>"""),
                  None,
                  "",
                  Map("id" -> "change")
                )
              )
            )
          )
        )
      )

      AgentClientDetailsViewModel.getSummaryRows(AgentClientDetails("CBCXX1", Some("Trading Name")))(messages(app)) mustBe expectedSummary
    }

  }
}

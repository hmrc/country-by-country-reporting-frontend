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

import models.AgentClientDetails
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

object AgentClientDetailsViewModel {

  def getSummaryListViewModel(summaryRows: Seq[SummaryListRow]) = SummaryListViewModel(summaryRows)
    .withoutBorders()
    .withCssClass("govuk-!-margin-bottom-0")

  def getSummaryRows(acd: AgentClientDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    val idRow = Seq(
      SummaryListRowViewModel(
        key = "agentClientDetails.id",
        value = ValueViewModel(HtmlFormat.escape(s"${acd.id}").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages(
              "manageYourClients.change.hidden"
            )}</span>"""),
            href = controllers.agent.routes.ManageYourClientsController.onPageLoad().url
          )
            .withAttribute(("id", "change"))
        )
      )
    )
    if (acd.tradingName.isDefined) {
      Seq(
        SummaryListRowViewModel(
          key = "agentClientDetails.name",
          value = ValueViewModel(HtmlFormat.escape(s"${acd.tradingName.get}").toString),
          actions = Seq()
        )
      ) ++ idRow
    } else { idRow }

  }
}

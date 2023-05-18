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

import controllers.agent.routes
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

class AgentCheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def getAgentPrimaryContactDetails: Seq[SummaryListRow] =
    Seq(firstContactNamePage(), firstContactEmailPage(), firstContactPhonePage()).flatten

  def getAgentSecondaryContactDetails: Seq[SummaryListRow] =
    Seq(hasSecondContactPage(), secondContactNamePage(), secondContactEmailPage(), secondContactPhonePage()).flatten

  def firstContactNamePage(): Option[SummaryListRow] = userAnswers.get(AgentFirstContactNamePage) map {
    x =>
      SummaryListRowViewModel(
        key = "agentFirstContactName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentFirstContactName.change.hidden")}</span>"""),
            href = routes.AgentFirstContactNameController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "contact-name"))
        )
      )
  }

  def firstContactEmailPage(): Option[SummaryListRow] = userAnswers.get(AgentFirstContactEmailPage) map {
    x =>
      SummaryListRowViewModel(
        key = "agentFirstContactEmail.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentFirstContactEmail.change.hidden")}</span>"""),
            href = routes.AgentFirstContactEmailController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "contact-email"))
        )
      )
  }

  def firstContactPhonePage(): Option[SummaryListRow] = {
    val summaryView = (value: String) =>
      SummaryListRowViewModel(
        key = "agentFirstContactPhone.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(value).toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentFirstContactPhone.change.hidden")}</span>"""),
            href = routes.AgentFirstContactHavePhoneController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "contact-phone"))
        )
      )

    Some(
      userAnswers.get(AgentFirstContactPhonePage) match {
        case Some(phone) if !phone.isEmpty => summaryView(phone)
        case _                             => summaryView(messages("no.phone"))
      }
    )
  }

  def hasSecondContactPage(): Option[SummaryListRow] = {
    val summaryView = (yesNo: String) =>
      SummaryListRowViewModel(
        key = "agentHaveSecondContact.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"${messages(yesNo)}").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentHaveSecondContact.change.hidden")}</span>"""),
            href = routes.AgentHaveSecondContactController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "second-contact"))
        )
      )
    Some(userAnswers.get(AgentHaveSecondContactPage) match {
      case Some(x) =>
        val yesNo = if (x) "site.yes" else "site.no"
        summaryView(yesNo)
      case None =>
        val yesNo = userAnswers.get(SecondContactNamePage) match {
          case Some(_) => "site.yes"
          case _       => "site.no"
        }
        summaryView(yesNo)
    })

  }

  def secondContactNamePage(): Option[SummaryListRow] = userAnswers.get(AgentSecondContactNamePage) map {
    x =>
      SummaryListRowViewModel(
        key = "agentSecondContactName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentSecondContactName.change.hidden")}</span>"""),
            href = routes.AgentSecondContactNameController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "snd-contact-name"))
        )
      )
  }

  def secondContactEmailPage(): Option[SummaryListRow] = userAnswers.get(AgentSecondContactEmailPage) map {
    x =>
      SummaryListRowViewModel(
        key = "agentSecondContactEmail.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentSecondContactEmail.change.hidden")}</span>"""),
            href = routes.AgentSecondContactEmailController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "snd-contact-email"))
        )
      )
  }

  def secondContactPhonePage(): Option[SummaryListRow] = {
    val summaryView = (value: String) =>
      SummaryListRowViewModel(
        key = "agentSecondContactPhone.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(value).toString),
        actions = Seq(
          ActionItemViewModel(
            content = HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span><span class="govuk-visually-hidden">${messages("agentSecondContactHavePhone.change.hidden")}</span>"""),
            href = routes.AgentSecondContactPhoneController.onPageLoad(CheckMode).url
          )
            .withAttribute(("id", "snd-contact-phone"))
        )
      )

    userAnswers.get(AgentHaveSecondContactPage) match {
      case Some(true) =>
        Some(
          userAnswers.get(AgentSecondContactPhonePage) match {
            case Some(phone) if !phone.isEmpty => summaryView(phone)
            case _                             => summaryView(messages("no.phone"))
          }
        )
      case _ => None
    }
  }
}

object AgentCheckYourAnswersHelper {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages) =
    new AgentCheckYourAnswersHelper(userAnswers)
}

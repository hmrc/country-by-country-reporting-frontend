package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AgentSecondContactHavePhonePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentSecondContactHavePhoneSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AgentSecondContactHavePhonePage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "agentSecondContactHavePhone.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AgentSecondContactHavePhoneController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("agentSecondContactHavePhone.change.hidden"))
          )
        )
    }
}

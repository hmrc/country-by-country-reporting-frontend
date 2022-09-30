package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.CanWeContactByEmailFirstPagePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CanWeContactByEmailFirstPageSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CanWeContactByEmailFirstPagePage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "canWeContactByEmailFirstPage.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.CanWeContactByEmailFirstPageController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("canWeContactByEmailFirstPage.change.hidden"))
          )
        )
    }
}

/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def getPrimaryContactDetails: Seq[SummaryListRow] =
    Seq(contactNamePage(), contactEmailPage(), contactPhonePage()).flatten

  def getSecondaryContactDetails: Seq[SummaryListRow] =
    Seq(hasSecondContactPage(), secondaryContactNamePage(), secondaryContactEmailPage(), secondaryContactPhonePage()).flatten

  def contactNamePage(): Option[SummaryListRow] = userAnswers.get(ContactNamePage) map {
    x =>
      SummaryListRowViewModel(
        key = "contactName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.ContactNameController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "contact-name"))
            .withVisuallyHiddenText(messages("contactName.change.hidden"))
        )
      )
  }

  def contactEmailPage(): Option[SummaryListRow] = userAnswers.get(ContactEmailPage) map {
    x =>
      SummaryListRowViewModel(
        key = "contactEmail.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.ContactEmailController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "contact-email"))
            .withVisuallyHiddenText(messages("contactEmail.change.hidden"))
        )
      )
  }

  def contactPhonePage(): Option[SummaryListRow] = {
    val summaryView = (value: String) =>
      SummaryListRowViewModel(
        key = "contactPhone.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(value).toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.HaveTelephoneController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "contact-phone"))
            .withVisuallyHiddenText(messages("contactPhone.change.hidden"))
        )
      )

    Some(
      userAnswers.get(ContactPhonePage) match {
        case Some(phone) if !phone.isEmpty => summaryView(phone)
        case _                             => summaryView(messages("no.phone"))
      }
    )
  }

  def hasSecondContactPage(): Option[SummaryListRow] = {
    val summaryView = (yesNo: String) =>
      SummaryListRowViewModel(
        key = "haveSecondContact.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"${messages(yesNo)}").toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.HaveSecondContactController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "second-contact"))
            .withVisuallyHiddenText(messages("haveSecondContact.change.hidden"))
        )
      )
    Some(userAnswers.get(HaveSecondContactPage) match {
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

  def secondaryContactNamePage(): Option[SummaryListRow] = userAnswers.get(SecondContactNamePage) map {
    x =>
      SummaryListRowViewModel(
        key = "secondContactName.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.SecondContactNameController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "snd-contact-name"))
            .withVisuallyHiddenText(messages("secondContactName.change.hidden"))
        )
      )
  }

  def secondaryContactEmailPage(): Option[SummaryListRow] = userAnswers.get(SecondContactEmailPage) map {
    x =>
      SummaryListRowViewModel(
        key = "secondContactEmail.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(s"$x").toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.SecondContactEmailController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "snd-contact-email"))
            .withVisuallyHiddenText(messages("secondContactEmail.change.hidden"))
        )
      )
  }

  def secondaryContactPhonePage(): Option[SummaryListRow] = {
    val summaryView = (value: String) =>
      SummaryListRowViewModel(
        key = "secondContactPhone.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(value).toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.SecondContactHavePhoneController.onPageLoad(CheckMode).url)
            .withAttribute(("id", "snd-contact-phone"))
            .withVisuallyHiddenText(messages("secondContactPhone.change.hidden"))
        )
      )

    userAnswers.get(HaveSecondContactPage) match {
      case Some(true) =>
        Some(
          userAnswers.get(SecondContactPhonePage) match {
            case Some(phone) if !phone.isEmpty => summaryView(phone)
            case _                             => summaryView(messages("no.phone"))
          }
        )
      case _ => None
    }
  }
}

object CheckYourAnswersHelper {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages) =
    new CheckYourAnswersHelper(userAnswers)
}

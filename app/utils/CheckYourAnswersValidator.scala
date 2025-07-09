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

package utils

import models.{Mode, UserAnswers}
import pages._
import play.api.libs.json.Reads

class CheckYourAnswersValidator(userAnswers: UserAnswers) {

  private def checkPage[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None => Some(page)
      case _    => None
    }

  private def checkPrimaryContactDetails: Seq[Page] = Seq(
    checkPage(ContactNamePage),
    checkPage(ContactEmailPage)
  ).flatten ++ checkPrimaryContactNumber

  private def checkPrimaryContactNumber: Seq[Page] = (userAnswers.get(HaveTelephonePage) match {
    case Some(true)  => checkPage(ContactPhonePage)
    case Some(false) => None
    case _           => Some(HaveTelephonePage)
  }).toSeq

  private def checkSecondaryContactPhone: Seq[Page] = (userAnswers.get(SecondContactHavePhonePage) match {
    case Some(true)  => checkPage(SecondContactPhonePage)
    case Some(false) => None
    case _           => Some(SecondContactHavePhonePage)
  }).toSeq

  private def checkSecondaryContactDetails: Seq[Page] =
    userAnswers.get(HaveSecondContactPage) match {
      case Some(true) =>
        Seq(
          checkPage(SecondContactNamePage),
          checkPage(SecondContactEmailPage)
        ).flatten ++ checkSecondaryContactPhone
      case Some(false) => Seq.empty
      case _           => Seq(HaveSecondContactPage)
    }

  private def validate: Seq[Page] = checkPrimaryContactDetails ++ checkSecondaryContactDetails

  private def pageToRedirectUrl(mode: Mode): Map[Page, String] = Map(
    ContactNamePage            -> controllers.routes.ContactNameController.onPageLoad(mode).url,
    ContactEmailPage           -> controllers.routes.ContactEmailController.onPageLoad(mode).url,
    HaveTelephonePage          -> controllers.routes.HaveTelephoneController.onPageLoad(mode).url,
    ContactPhonePage           -> controllers.routes.HaveTelephoneController.onPageLoad(mode).url,
    HaveSecondContactPage      -> controllers.routes.HaveSecondContactController.onPageLoad(mode).url,
    SecondContactNamePage      -> controllers.routes.HaveSecondContactController.onPageLoad(mode).url,
    SecondContactEmailPage     -> controllers.routes.SecondContactEmailController.onPageLoad(mode).url,
    SecondContactHavePhonePage -> controllers.routes.SecondContactHavePhoneController.onPageLoad(mode).url,
    SecondContactPhonePage     -> controllers.routes.SecondContactHavePhoneController.onPageLoad(mode).url
  )

  def changeAnswersRedirectUrl(mode: Mode): Option[String] =
    validate.headOption
      .map(pageToRedirectUrl(mode))
}

object CheckYourAnswersValidator {

  def apply(userAnswers: UserAnswers) =
    new CheckYourAnswersValidator(userAnswers)
}

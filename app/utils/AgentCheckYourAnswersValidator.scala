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

class AgentCheckYourAnswersValidator(userAnswers: UserAnswers) {

  private def checkPage[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[Page] =
    userAnswers.get(page) match {
      case None => Some(page)
      case _    => None
    }

  private def checkPrimaryContactDetails: Seq[Page] = Seq(
    checkPage(AgentFirstContactNamePage),
    checkPage(AgentFirstContactEmailPage)
  ).flatten ++ checkPrimaryContactNumber

  private def checkPrimaryContactNumber: Seq[Page] = (userAnswers.get(AgentFirstContactHavePhonePage) match {
    case Some(true)  => checkPage(AgentFirstContactPhonePage)
    case Some(false) => None
    case _           => Some(AgentFirstContactHavePhonePage)
  }).toSeq

  private def checkSecondaryContactPhone: Seq[Page] = (userAnswers.get(AgentSecondContactHavePhonePage) match {
    case Some(true)  => checkPage(AgentSecondContactPhonePage)
    case Some(false) => None
    case _           => Some(AgentSecondContactHavePhonePage)
  }).toSeq

  private def checkSecondaryContactDetails: Seq[Page] =
    userAnswers.get(AgentHaveSecondContactPage) match {
      case Some(true) =>
        Seq(
          checkPage(AgentSecondContactNamePage),
          checkPage(AgentSecondContactEmailPage)
        ).flatten ++ checkSecondaryContactPhone
      case Some(false) => Seq.empty
      case _           => Seq(AgentHaveSecondContactPage)
    }

  private def checkMigratedAgentAnswerUpdated: Seq[Page] =
    userAnswers.get(IsMigratedAgentContactUpdatedPage) match {
      case Some(false) =>
        Seq(IsMigratedAgentContactUpdatedPage)
      case _ => Seq.empty
    }

  private def validate: Seq[Page] = checkPrimaryContactDetails ++ checkSecondaryContactDetails ++ checkMigratedAgentAnswerUpdated

  private def pageToRedirectUrl(mode: Mode): Map[Page, String] = Map(
    AgentFirstContactNamePage         -> controllers.agent.routes.AgentFirstContactNameController.onPageLoad(mode).url,
    AgentFirstContactEmailPage        -> controllers.agent.routes.AgentFirstContactEmailController.onPageLoad(mode).url,
    AgentFirstContactHavePhonePage    -> controllers.agent.routes.AgentFirstContactHavePhoneController.onPageLoad(mode).url,
    AgentFirstContactPhonePage        -> controllers.agent.routes.AgentFirstContactHavePhoneController.onPageLoad(mode).url,
    AgentHaveSecondContactPage        -> controllers.agent.routes.AgentHaveSecondContactController.onPageLoad(mode).url,
    AgentSecondContactNamePage        -> controllers.agent.routes.AgentHaveSecondContactController.onPageLoad(mode).url,
    AgentSecondContactEmailPage       -> controllers.agent.routes.AgentSecondContactEmailController.onPageLoad(mode).url,
    AgentSecondContactHavePhonePage   -> controllers.agent.routes.AgentSecondContactHavePhoneController.onPageLoad(mode).url,
    AgentSecondContactPhonePage       -> controllers.agent.routes.AgentSecondContactHavePhoneController.onPageLoad(mode).url,
    IsMigratedAgentContactUpdatedPage -> controllers.agent.routes.ChangeAgentContactDetailsController.onPageLoad().url
  )

  def changeAnswersRedirectUrl(mode: Mode): Option[String] =
    validate.headOption
      .map(pageToRedirectUrl(mode))
}

object AgentCheckYourAnswersValidator {

  def apply(userAnswers: UserAnswers) =
    new AgentCheckYourAnswersValidator(userAnswers)
}

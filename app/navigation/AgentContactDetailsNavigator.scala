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

package navigation

import controllers.agent.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class AgentContactDetailsNavigator @Inject() () {

  val normalRoutes: (Page) => UserAnswers => Call = {
    case AgentFirstContactNamePage      => _ => routes.AgentFirstContactEmailController.onPageLoad(NormalMode)
    case AgentFirstContactEmailPage     => _ => routes.AgentFirstContactHavePhoneController.onPageLoad(NormalMode)
    case AgentFirstContactHavePhonePage => ua => haveTelephoneRoutes(NormalMode)(ua)
    case AgentFirstContactPhonePage     => _ => routes.AgentHaveSecondContactController.onPageLoad(NormalMode)
    case AgentHaveSecondContactPage =>
      ua =>
        yesNoPage(
          ua,
          AgentHaveSecondContactPage,
          routes.AgentSecondContactNameController.onPageLoad(NormalMode),
          routes.ChangeAgentContactDetailsController.onPageLoad()
        )
    case AgentSecondContactNamePage  => _ => routes.AgentSecondContactEmailController.onPageLoad(NormalMode)
    case AgentSecondContactEmailPage => _ => routes.AgentSecondContactHavePhoneController.onPageLoad(NormalMode)
    case AgentSecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          AgentSecondContactHavePhonePage,
          routes.AgentSecondContactPhoneController.onPageLoad(NormalMode),
          routes.ChangeAgentContactDetailsController.onPageLoad()
        )
    case AgentSecondContactPhonePage => _ => routes.ChangeAgentContactDetailsController.onPageLoad()
    case _                           => _ => controllers.routes.ThereIsAProblemController.onPageLoad()
  }

  val checkRouteMap: Page => UserAnswers => Call = {
    case AgentFirstContactNamePage      => _ => routes.AgentFirstContactEmailController.onPageLoad(CheckMode)
    case AgentFirstContactEmailPage     => _ => routes.AgentFirstContactHavePhoneController.onPageLoad(CheckMode)
    case AgentFirstContactHavePhonePage => ua => haveTelephoneRoutes(CheckMode)(ua)
    case AgentFirstContactPhonePage     => _ => routes.ChangeAgentContactDetailsController.onPageLoad()
    case AgentHaveSecondContactPage =>
      ua =>
        yesNoPage(
          ua,
          AgentHaveSecondContactPage,
          routes.AgentSecondContactNameController.onPageLoad(CheckMode),
          routes.ChangeAgentContactDetailsController.onPageLoad()
        )
    case AgentSecondContactNamePage  => _ => routes.AgentSecondContactEmailController.onPageLoad(CheckMode)
    case AgentSecondContactEmailPage => _ => routes.AgentSecondContactHavePhoneController.onPageLoad(CheckMode)
    case AgentSecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          AgentSecondContactHavePhonePage,
          routes.AgentSecondContactPhoneController.onPageLoad(CheckMode),
          routes.ChangeAgentContactDetailsController.onPageLoad()
        )
    case AgentSecondContactPhonePage => _ => routes.ChangeAgentContactDetailsController.onPageLoad()
    case _                           => _ => controllers.routes.ThereIsAProblemController.onPageLoad()
  }

  private def haveTelephoneRoutes(mode: Mode)(ua: UserAnswers): Call =
    ua.get(AgentFirstContactHavePhonePage) match {
      case Some(hasPhone) if hasPhone =>
        routes.AgentFirstContactPhoneController.onPageLoad(mode)
      case _ =>
        nextPage(AgentFirstContactPhonePage, mode, ua)
    }

  def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.ThereIsAProblemController.onPageLoad())

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}

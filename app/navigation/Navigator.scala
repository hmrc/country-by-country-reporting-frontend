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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import models.ManageYourClients.{AddAClientToYourAgentServicesAccount, ChangeYourCBCAgentContactDetails, SelectAClient}
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case InvalidXMLPage        => _ => routes.FileDataErrorController.onPageLoad()
    case ValidXMLPage          => _ => routes.CheckYourFileDetailsController.onPageLoad()
    case ManageYourClientsPage => ua => whatToDoNextNavigation(ua)
    case AgentIsThisYourClientPage =>
      ua => yesNoPage(ua, AgentIsThisYourClientPage, routes.IndexController.onPageLoad, controllers.client.routes.ProblemCBCIdController.onPageLoad())
    case _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.IndexController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.ThereIsAProblemController.onPageLoad())

  def whatToDoNextNavigation(ua: UserAnswers) =
    ua.get(ManageYourClientsPage)
      .map {
        case SelectAClient                        => controllers.agent.routes.AgentClientIdController.onPageLoad()
        case AddAClientToYourAgentServicesAccount => controllers.agent.routes.ManageYourClientsController.onPageLoad()
        case ChangeYourCBCAgentContactDetails     => controllers.agent.routes.ChangeAgentContactDetailsController.onPageLoad()
      }
      .getOrElse(routes.ThereIsAProblemController.onPageLoad())
}

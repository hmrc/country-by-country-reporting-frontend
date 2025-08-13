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

package controllers.agent

import controllers.actions.agent.{AgentDataRequiredAction, AgentDataRetrievalAction, AgentIdentifierAction}
import models.{CheckMode, NormalMode}
import pages.JourneyInProgressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AgentCheckYourAnswersValidator
import views.html.SomeInformationMissingView

import javax.inject.Inject

class AgentSomeInformationMissingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: AgentIdentifierAction,
  getData: AgentDataRetrievalAction,
  requireData: AgentDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: SomeInformationMissingView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      val answers  = request.userAnswers
      val validate = AgentCheckYourAnswersValidator(answers)
      val mode     = if (answers.get(JourneyInProgressPage).getOrElse(false)) CheckMode else NormalMode

      val redirectUrl = validate.changeAnswersRedirectUrl(mode) match {
        case Some(value) => value
        case None        => controllers.agent.routes.AgentFirstContactNameController.onPageLoad(NormalMode).url
      }
      Ok(view(redirectUrl))
  }
}

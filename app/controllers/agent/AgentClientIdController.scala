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

package controllers.agent

import controllers.actions.agent.{AgentDataRequiredAction, AgentDataRetrievalAction, AgentIdentifierAction}
import forms.AgentClientIdFormProvider
import models.{NormalMode, UserAnswers}
import navigation.AgentContactDetailsNavigator
import pages.{AgentClientIdPage, AgentIsThisYourClientPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.AgentClientIdView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentClientIdController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: AgentContactDetailsNavigator,
  identifier: AgentIdentifierAction,
  view: AgentClientIdView,
  formProvider: AgentClientIdFormProvider,
  getData: AgentDataRetrievalAction,
  requireData: AgentDataRequiredAction,
  override val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository
)(implicit
  executionContext: ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identifier andThen getData() andThen requireData) {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = (identifier andThen getData() andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest((view(formWithErrors)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentClientIdPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.AgentIsThisYourClientController.onPageLoad)
        )
  }
}

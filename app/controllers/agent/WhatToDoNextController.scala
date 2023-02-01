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

import controllers.actions._
import controllers.actions.agent.{AgentDataRequiredAction, AgentDataRetrievalAction, AgentIdentifierAction}
import controllers.routes
import forms.WhatToDoNextFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{JourneyInProgressPage, WhatToDoNextPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AgentSubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatToDoNextView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatToDoNextController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: AgentIdentifierAction,
  getData: AgentDataRetrievalAction,
  requireData: AgentDataRequiredAction,
  formProvider: WhatToDoNextFormProvider,
  agentSubscriptionService: AgentSubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  view: WhatToDoNextView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData.apply).async {
    implicit request =>
      Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(JourneyInProgressPage, true)).flatMap {
        ua =>
          sessionRepository.set(ua).flatMap {
            _ =>
              agentSubscriptionService.getAgentContactDetails(ua) flatMap {
                case Some(agentUserAnswers) =>
                  sessionRepository.set(agentUserAnswers).map {
                    _ =>
                      val preparedForm = ua.get(WhatToDoNextPage) match {
                        case None        => form
                        case Some(value) => form.fill(value)
                      }
                      Ok(view(preparedForm, mode))
                  }
                case _ =>
                  Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
              }
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatToDoNextPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatToDoNextPage, mode, updatedAnswers))
        )
  }
}

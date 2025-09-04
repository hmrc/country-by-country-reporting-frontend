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
import pages.{AgentClientIdPage, IsMigratedAgentContactUpdatedPage, JourneyInProgressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.AgentContactDetailsUpdatedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentContactDetailsUpdatedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: AgentIdentifierAction,
  getData: AgentDataRetrievalAction,
  requireData: AgentDataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: AgentContactDetailsUpdatedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      for {
        a <- Future.fromTry(request.userAnswers.remove(JourneyInProgressPage))
        b <- Future.fromTry(a.set(IsMigratedAgentContactUpdatedPage, true))
      } yield {
        if (request.userAnswers.get(IsMigratedAgentContactUpdatedPage).isDefined) {
          sessionRepository.set(b)
        } else {
          sessionRepository.set(a)
        }
        val clientSelected = request.userAnswers.get(AgentClientIdPage).isDefined
        Ok(view(clientSelected))
      }
  }
}

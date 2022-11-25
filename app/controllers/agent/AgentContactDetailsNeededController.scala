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

package controllers.agent

import controllers.actions._
import controllers.actions.agent.{AgentDataRetrievalAction, AgentIdentifierAction}
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.AgentContactDetailsNeededView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentContactDetailsNeededController @Inject() (
  override val messagesApi: MessagesApi,
  identify: AgentIdentifierAction,
  getData: AgentDataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: AgentContactDetailsNeededView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData()) {
    implicit request =>
      Ok(view())
  }
}

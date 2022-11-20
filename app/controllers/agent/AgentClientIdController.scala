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

import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.i18n.I18nSupport
import controllers.actions.IdentifierAction
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import views.html.agent.AgentClientIdView
import forms.AgentClientIdFormProvider
import com.google.inject.Inject
import akka.compat.Future
import scala.concurrent.Future
import navigation.AgentContactDetailsNavigator
import models.NormalMode
import repositories.SessionRepository
import controllers.actions.DataRetrievalAction

class AgentClientIdController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: AgentContactDetailsNavigator,
  identifier: IdentifierAction,
  view: AgentClientIdView,
  formProvider: AgentClientIdFormProvider,
  override val controllerComponents: MessagesControllerComponents,
  sessionRespository: SessionRepository,
  dataRetrieval: DataRetrievalAction
) extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = identifier {
    implicit request => Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = identifier async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest((view(formWithErrors)))),
          value => {
            sessionRespository.clear(request.userId)
            Future.successful(Redirect(routes.AgentFirstContactNameController.onPageLoad(NormalMode)).withHeaders(("clientId", value)))
          }
        )
  }
}

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

package controllers.client

import controllers.actions._
import forms.{AgentFirstContactEmailFormProvider, ContactEmailFormProvider}
import models.Mode
import navigation.ContactDetailsNavigator
import pages.{AgentFirstContactNamePage, ContactEmailPage, ContactNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.client
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.client.ClientFirstContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientFirstContactEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ContactEmailFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClientFirstContactEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider("contactEmail")

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ContactNamePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getPluralAgentFirstContactName(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName = getPluralAgentFirstContactName(request.userAnswers)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactEmailPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ContactEmailPage, mode, updatedAnswers))
        )
  }
}
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
import forms.HaveSecondContactFormProvider
import models.Mode
import navigation.ClientContactDetailsNavigator
import pages.HaveSecondContactPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.client.ClientHaveSecondContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientHaveSecondContactController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ClientContactDetailsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: HaveSecondContactFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ClientHaveSecondContactView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider("clientHaveSecondContact")

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(HaveSecondContactPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      Ok(view(preparedForm, mode, getFirstContactName(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, getFirstContactName(request.userAnswers)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(HaveSecondContactPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(HaveSecondContactPage, mode, updatedAnswers))
        )
  }
}

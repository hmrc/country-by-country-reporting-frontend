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

package controllers

import controllers.actions._
import forms.SecondContactPhoneFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{SecondContactNamePage, SecondContactPhonePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecondContactPhoneView

import scala.concurrent.{ExecutionContext, Future}

class SecondContactPhoneController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: SecondContactPhoneFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: SecondContactPhoneView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(SecondContactPhonePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, getSecondContactName(request.userAnswers), mode))
  }

  private def getSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    (userAnswers.get(SecondContactNamePage)) match {
      case Some(contactName) => contactName
      case _                 => messages("default.secondContact.name")
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, getSecondContactName(request.userAnswers), mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondContactPhonePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SecondContactPhonePage, mode, updatedAnswers))
      )
  }
}

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

package controllers.client

import controllers.actions._
import controllers.routes
import forms.ReviewClientContactDetailsFormProvider
import models.NormalMode
import navigation.ClientContactDetailsNavigator
import pages.{PrimaryClientContactInformationPage, ReviewClientContactDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.client.ReviewClientContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewClientContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ClientContactDetailsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReviewClientContactDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReviewClientContactDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ReviewDetailsHelper {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ReviewClientContactDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(PrimaryClientContactInformationPage) match {
        case None =>
          logger.warn("Contact information is not available")
          Redirect(controllers.routes.ThereIsAProblemController.onPageLoad())
        case Some(contactDetails) => Ok(view(preparedForm, contactDetails, NormalMode))
      }

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers
              .get(PrimaryClientContactInformationPage)
              .fold {
                Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
              } {
                contactInformation => Future.successful(BadRequest(view(formWithErrors, contactInformation, NormalMode)))
              },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ReviewClientContactDetailsPage, value))
              populatedAnswers = populateUserAnswers(updatedAnswers, value)
              _ <- sessionRepository.set(populatedAnswers)
            } yield Redirect(navigator.nextPage(ReviewClientContactDetailsPage, NormalMode, populatedAnswers))
        )
  }

}

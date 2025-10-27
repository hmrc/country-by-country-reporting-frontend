/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.client.ReviewDetailsHelper
import forms.ReviewContactDetailsFormProvider
import models.NormalMode
import navigation.ContactDetailsNavigator
import pages.{PrimaryClientContactInformationPage, ReviewContactDetailsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReviewContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ContactDetailsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReviewContactDetailsFormProvider,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  view: ReviewContactDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ReviewDetailsHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(ReviewContactDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      subscriptionService.getContactDetails(request.userAnswers, request.subscriptionId).map {
        case Some(userAnswers) =>
          userAnswers.get(PrimaryClientContactInformationPage) match {
            case Some(migratedContactDetails) => Ok(view(preparedForm, migratedContactDetails))
            case None =>
              println(Console.BLUE + "deets got but still broke." + Console.RESET)

              Redirect(routes.ThereIsAProblemController.onPageLoad())
          }
        case None =>
          println(Console.BLUE + "No contact details found for the user." + Console.RESET)
          Redirect(routes.ThereIsAProblemController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      subscriptionService.getContactDetails(request.userAnswers, request.subscriptionId).flatMap {
        _.flatMap(_.get(PrimaryClientContactInformationPage)) match {
          case Some(migratedContactDetails) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, migratedContactDetails))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(ReviewContactDetailsPage, value))
                    populatedAnswers = populateUserAnswers(updatedAnswers, value)
                    _ <- sessionRepository.set(populatedAnswers)
                  } yield Redirect(navigator.nextPage(ReviewContactDetailsPage, NormalMode, updatedAnswers))
              )

          case None => Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
        }
      }

  }

}

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

package controllers

import connectors.FileDetailsConnector
import controllers.actions._
import models.fileDetails.{FileValidationErrors, Pending, Rejected, RejectedSDES, RejectedSDESVirus, Accepted => FileStatusAccepted}
import models.{UserAnswers, ValidatedFileData}
import pages.{ConversationIdPage, UploadIDPage, ValidXMLPage}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FileProblemHelper.isProblemStatus
import viewmodels.FileCheckViewModel
import views.html.{FilePendingChecksView, ThereIsAProblemView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FilePendingChecksController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileConnector: FileDetailsConnector,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: FilePendingChecksView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(ValidXMLPage), request.userAnswers.get(ConversationIdPage)) match {
        case (Some(xmlDetails), Some(conversationId)) =>
          fileConnector.getStatus(conversationId) flatMap {
            case Some(FileStatusAccepted) =>
              Future.successful(Redirect(routes.FilePassedChecksController.onPageLoad()))
            case Some(Rejected(errors)) =>
              slowJourneyErrorRoute(
                errors,
                Future.successful(Redirect(routes.FileFailedChecksController.onPageLoad()))
              )
            case Some(Pending) => handlePendingFile(xmlDetails, request.userAnswers, request.isAgent)
            case Some(RejectedSDES) =>
              Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
            case Some(RejectedSDESVirus) =>
              Future.successful(Redirect(routes.FileProblemVirusController.onPageLoad()))
            case _ =>
              logger.warn("Unable to get Status")
              Future.successful(InternalServerError(errorView()))
          }
        case _ =>
          logger.warn("Unable to retrieve fileName & conversationId")
          Future.successful(InternalServerError(errorView()))
      }
  }

  private def handlePendingFile(xmlDetails: ValidatedFileData, userAnswers: UserAnswers, isAgent: Boolean)(implicit request: Request[_]) = {
    val summary = FileCheckViewModel.createFileSummary(xmlDetails.messageSpecData.messageRefId, Pending.toString)
    userAnswers.get(ConversationIdPage) match {
      case Some(conversationId) =>
        for {
          updatedAnswers <- Future.fromTry(userAnswers.remove(UploadIDPage))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Ok(view(summary, routes.FilePendingChecksController.onPageLoad().url, conversationId.value, isAgent))
      case _ => Future.successful(InternalServerError(errorView()))
    }
  }

  private def slowJourneyErrorRoute(errors: FileValidationErrors, result: Future[Result]): Future[Result] =
    if (isProblemStatus(errors)) {
      Future.successful(Redirect(routes.FileProblemController.onPageLoad()))
    } else {
      result
    }
}

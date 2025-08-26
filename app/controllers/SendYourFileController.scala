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

import config.FrontendAppConfig
import connectors.{FileDetailsConnector, SubmissionConnector}
import controllers.actions._
import models.ValidatedFileData
import models.fileDetails.{FileValidationErrors, Pending, Rejected, RejectedSDES, RejectedSDESVirus, Accepted => FileStatusAccepted}
import models.submission.SubmissionDetails
import models.upscan.URL
import pages._
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FileProblemHelper.isProblemStatus
import viewmodels.SendYourFileViewModel
import views.html.SendYourFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SendYourFileController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkForSubmission: CheckForSubmissionAction,
  appConfig: FrontendAppConfig,
  submissionConnector: SubmissionConnector,
  fileDetailsConnector: FileDetailsConnector,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: SendYourFileView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData andThen checkForSubmission(true)).async {
    implicit request =>
      request.userAnswers
        .get(ValidXMLPage)
        .fold(
          Future.successful(Redirect(controllers.routes.FileProblemSomeInformationMissingController.onPageLoad()))
        ) {
          validXMLData =>
            val reportType = validXMLData.messageSpecData.reportType
            Future.successful(Ok(view(appConfig, SendYourFileViewModel.getWarningText(reportType))))
        }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(ValidXMLPage),
       request.userAnswers.get(URLPage),
       request.userAnswers.get(UploadIDPage),
       request.userAnswers.get(FileReferencePage)
      ) match {
        case (Some(ValidatedFileData(fileName, messageSpecData, fileSize, checksum)), Some(fileUrl), Some(uploadId), Some(fileReference)) =>
          val submissionDetails = SubmissionDetails(fileName, uploadId, request.subscriptionId, fileSize, fileUrl, checksum, messageSpecData, fileReference)
          submissionConnector.submitDocument(submissionDetails)(hc, ec) flatMap {
            case Some(conversationId) =>
              for {
                userAnswers <- Future.fromTry(request.userAnswers.set(ConversationIdPage, conversationId))
                _           <- sessionRepository.set(userAnswers)
              } yield Redirect(controllers.routes.FilePendingChecksController.onPageLoad())
            case _ => Future.successful(InternalServerError)
          }
        case (None, _, _, _) =>
          Future.successful(Redirect(controllers.routes.FileProblemSomeInformationMissingController.onPageLoad()))
        case _ =>
          Future.successful(InternalServerError)
      }
  }

  def getStatus: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ConversationIdPage) match {
        case Some(conversationId) =>
          fileDetailsConnector.getStatus(conversationId) flatMap {
            case Some(FileStatusAccepted) =>
              Future.successful(Ok(Json.toJson(URL(controllers.routes.FileReceivedController.onPageLoad(conversationId).url))))
            case Some(Rejected(errors)) =>
              fastJourneyErrorRoute(
                errors,
                Future.successful(Ok(Json.toJson(URL(controllers.routes.FileRejectedController.onPageLoad(conversationId).url))))
              )
            case Some(Pending) =>
              Future.successful(NoContent)
            case Some(RejectedSDES) =>
              Future.successful(Ok(Json.toJson(URL(controllers.routes.ThereIsAProblemController.onPageLoad().url))))
            case Some(RejectedSDESVirus) =>
              Future.successful(Ok(Json.toJson(URL(controllers.routes.FileProblemVirusController.onPageLoad().url))))
            case None =>
              logger.warn("getStatus: no status returned")
              Future.successful(InternalServerError)
          }
        case None =>
          logger.warn("UserAnswers.ConversationId is empty")
          Future.successful(InternalServerError)
      }
  }

  private def fastJourneyErrorRoute(errors: FileValidationErrors, result: Future[Result]): Future[Result] =
    if (isProblemStatus(errors)) {
      Future.successful(Ok(Json.toJson(URL(controllers.routes.FileProblemSomeInformationMissingController.onPageLoad().url))))
    } else {
      result
    }

}

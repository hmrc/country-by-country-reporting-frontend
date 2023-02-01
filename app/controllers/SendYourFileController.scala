/*
 * Copyright 2023 HM Revenue & Customs
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
import handlers.XmlHandler
import models.fileDetails.{Pending, Rejected, ValidationErrors, Accepted => FileStatusAccepted}
import models.upscan.URL
import models.{CBC402, ValidatedFileData}
import pages.{ConversationIdPage, URLPage, ValidXMLPage}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FileProblemHelper.isProblemStatus
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
  xmlHandler: XmlHandler,
  val controllerComponents: MessagesControllerComponents,
  view: SendYourFileView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData andThen checkForSubmission(true)).async {
    implicit request =>
      val displayWarning = request.userAnswers
        .get(ValidXMLPage)
        .fold(false)(
          validatedFileData => validatedFileData.messageSpecData.messageTypeIndic.equals(CBC402)
        )
      Future.successful(Ok(view(displayWarning, appConfig)))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(ValidXMLPage), request.userAnswers.get(URLPage)) match {
        case (Some(ValidatedFileData(filename, _)), Some(fileUrl)) =>
          val xml = xmlHandler.load(fileUrl)
          submissionConnector.submitDocument(filename, request.subscriptionId, xml) flatMap {
            case Some(conversationId) =>
              for {
                userAnswers <- Future.fromTry(request.userAnswers.set(ConversationIdPage, conversationId))
                _           <- sessionRepository.set(userAnswers)
              } yield Ok
            case _ => Future.successful(InternalServerError)
          }
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
              Future.successful(Ok(Json.toJson(URL(routes.FileReceivedController.onPageLoad(conversationId).url))))
            case Some(Rejected(errors)) =>
              fastJourneyErrorRoute(
                errors,
                Future.successful(Ok(Json.toJson(URL(routes.FileRejectedController.onPageLoad(conversationId).url))))
              )
            case Some(Pending) =>
              Future.successful(NoContent)
            case None =>
              logger.warn("getStatus: no status returned")
              Future.successful(InternalServerError)
          }
        case None =>
          logger.warn("UserAnswers.ConversationId is empty")
          Future.successful(InternalServerError)
      }
  }

  private def fastJourneyErrorRoute(errors: ValidationErrors, result: Future[Result]): Future[Result] =
    if (isProblemStatus(errors)) {
      Future.successful(Ok(Json.toJson(URL(routes.FileProblemController.onPageLoad().url))))
    } else {
      result
    }

}

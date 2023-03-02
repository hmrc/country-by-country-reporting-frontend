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

import connectors.FileDetailsConnector
import controllers.actions._
import models.ConversationId
import pages.UploadIDPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import viewmodels.FileReceivedViewModel
import views.html.{FileReceivedAgentView, FileReceivedView, ThereIsAProblemView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileReceivedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileDetailsConnector: FileDetailsConnector,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: FileReceivedView,
  agentView: FileReceivedAgentView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper
    with Logging {

  def onPageLoad(conversationId: ConversationId): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      fileDetailsConnector.getFileDetails(conversationId) flatMap {
        fileDetails =>
          (for {
            emails  <- getContactEmails
            details <- fileDetails
          } yield request.userType match {
            case AffinityGroup.Agent =>
              getAgentContactEmails match {
                case Some(agentContactEmails) =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.remove(UploadIDPage))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Ok(
                    agentView(
                      FileReceivedViewModel.formattedSummaryListView(FileReceivedViewModel.getAgentSummaryRows(details)),
                      emails.firstContact,
                      emails.secondContact,
                      agentContactEmails.firstContact,
                      agentContactEmails.secondContact
                    )
                  )
                case None =>
                  logger.warn("FileReceivedController: Agent detected but cannot retrieve agent email")
                  Future.successful(InternalServerError(errorView()))
              }
            case Organisation =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.remove(UploadIDPage))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Ok(
                view(FileReceivedViewModel.formattedSummaryListView(FileReceivedViewModel.getSummaryRows(details)), emails.firstContact, emails.secondContact)
              )
            case _ =>
              logger.warn("FileReceivedController: The User is neither an Organisation or an Agent")
              Future.successful(InternalServerError(errorView()))
          }).getOrElse(Future.successful(InternalServerError(errorView())))
      }
  }
}

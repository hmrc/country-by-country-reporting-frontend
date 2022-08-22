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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models._
import pages.{ConversationIdPage, ValidXMLPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.FileCheckViewModel
import views.html.{FilePassedChecksView, ThereIsAProblemView}
import models.{CBC401, ConversationId, MessageSpecData, ValidatedFileData}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FilePassedChecksController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: FilePassedChecksView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      (request.userAnswers.get(ValidXMLPage), request.userAnswers.get(ConversationIdPage)) match {
        case (Some(xmlDetails), Some(conversationId)) =>
          val action  = routes.FileReceivedController.onPageLoad(conversationId).url
          val summary = FileCheckViewModel.createFileSummary(xmlDetails.fileName, "Accepted")
          Ok(view(summary, action))

        case _ =>
          logger.warn("FilePassedChecksController: Unable to retrieve either XML information or ConversationId from UserAnswers")
          InternalServerError(errorView())
      }
  }

  //ToDo remove when no longer necessary and remove routes
  def testInvalidFileChecksPassed = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val validXmlDetails = ValidatedFileData("name", MessageSpecData("messageRefId", CBC401))
      val conversationId  = ConversationId("conversationId")
      for {
        updatedAnswers             <- Future.fromTry(request.userAnswers.set(ValidXMLPage, validXmlDetails))
        updatedAnswersConversation <- Future.fromTry(updatedAnswers.set(ConversationIdPage, conversationId))
        _                          <- sessionRepository.set(updatedAnswersConversation)
      } yield Redirect(routes.FilePassedChecksController.onPageLoad.url)
  }
}

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
import models.fileDetails.Rejected
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.FileRejectedViewModel
import views.html.{FileRejectedView, ThereIsAProblemView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileRejectedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: FileRejectedView,
  errorView: ThereIsAProblemView,
  fileDetailsConnector: FileDetailsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(conversationId: ConversationId): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      fileDetailsConnector.getFileDetails(conversationId) map {
        case Some(details) =>
          details.status match {
            case Rejected(validationErrors) =>
              Ok(view(details.name, FileRejectedViewModel.createTable(validationErrors)))
            case _ => InternalServerError(errorView())
          }
        case _ => InternalServerError(errorView())
      }
  }
}

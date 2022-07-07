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
import models.ConversationId
import models.fileDetails.FileDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactEmailHelper.getContactEmails
import utils.DateTimeFormatUtil._
import views.html.{FileReceivedView, ThereIsAProblemView}
import models.fileDetails.{Accepted => FileAccepted}
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileReceivedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: FileReceivedView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(conversationId: ConversationId): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      Future.successful(Some(FileDetails("fileName.xml", "messageRefId", LocalDateTime.now(), LocalDateTime.now(), FileAccepted, conversationId))) map {
//      fileDetailsConnector.getFileDetails(conversationId) map {
        fileDetails =>
          (for {
            emails  <- getContactEmails
            details <- fileDetails
          } yield {
            val time = details.submitted.format(timeFormatter).toLowerCase
            val date = details.submitted.format(dateFormatter)

            Ok(view(details.messageRefId, time, date, emails.firstContact, emails.secondContact))
          }).getOrElse(InternalServerError(errorView()))
      }
  }
}

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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import connectors.SubmissionConnector
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class TestSubmissionController @Inject() (
  identifierAction: IdentifierAction,
  connector: SubmissionConnector,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def insertTestSubmission(fileName: String): Action[NodeSeq] = identifierAction(parse.xml).async {
    implicit request =>
      logger.debug(s"inserting test submission: ${request.body}")
      connector
        .submitDocument(fileName, request.subscriptionId, request.body)
        .map {
          case Some(conversationId) => Ok(Json.toJson(conversationId))
          case _                    => InternalServerError
        }
  }

}

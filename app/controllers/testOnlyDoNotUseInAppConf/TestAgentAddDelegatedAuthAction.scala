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

package controllers.testOnlyDoNotUseInAppConf

import controllers.routes
import models.UserAnswers
import pages.AgentClientIdPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestAgentAddDelegatedAuthAction @Inject() (
  override val authConnector: AuthConnector,
  sessionRepository: SessionRepository,
  override val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent]
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.internalId) {
        case Some(userId) =>
          Future.fromTry(UserAnswers(userId).set(AgentClientIdPage, "XACBC0000123778")).flatMap { updatedAnswers =>
            sessionRepository.set(updatedAnswers).flatMap { _ =>
              block(request)
            }
          }
        case _ =>
          logger.warn("AgentIdentifierAction: Unable to retrieve internal id")
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
      } recover { case _ =>
      Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

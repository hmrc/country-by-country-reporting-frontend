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

package controllers.actions.agent

import javax.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.agent.AgentIdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import scala.concurrent.{ExecutionContext, Future}

trait AgentIdentifierAction extends ActionBuilder[AgentIdentifierRequest, AnyContent]

class AuthenticatedAgentIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  override val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AgentIdentifierAction
    with AuthorisedFunctions
    with Logging {

  val AGENT_ENROLMENT_ID: String = "HMRC-AS-AGENT"

  override def invokeBlock[A](request: Request[A], block: AgentIdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and Retrievals.affinityGroup) {
        case Some(userId) ~ enrolments ~ Some(Agent) =>
          enrolments.getEnrolment(AGENT_ENROLMENT_ID) match {
            case Some(Enrolment(AGENT_ENROLMENT_ID, Seq(EnrolmentIdentifier(_, arn)), _, _)) =>
              logger.debug("AgentIdentifierAction: Authenticated as an Agent")
              block(AgentIdentifierRequest(request, userId, arn))
            case _ =>
              logger.debug(s"AgentIdentifierAction: Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
              Future.successful(Redirect(controllers.agent.routes.AgentUseAgentServicesController.onPageLoad))
          }
        case _ ~ _ ~ Some(affinityGroup) =>
          logger.debug(s"AgentIdentifierAction: Affinity group not Agent. Affinity group: $affinityGroup")
          Future.successful(Redirect(routes.IndexController.onPageLoad))
        case _ =>
          logger.warn("AgentIdentifierAction: Unable to retrieve internal id or affinity group")
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
      } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

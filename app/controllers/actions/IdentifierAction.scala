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

package controllers.actions

import javax.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.UserAnswers
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import repositories.SessionRepository
import pages.AgentClientIdPage
import play.api.libs.json.Json
import services.AgentSubscriptionService

trait IdentifierAction
    extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  agentSubscriptionService: AgentSubscriptionService,
  val parser: BodyParsers.Default,
  sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and Retrievals.affinityGroup) {
        case Some(_) ~ _ ~ Some(Individual)                 => Future.successful(Left(Redirect(routes.IndividualSignInProblemController.onPageLoad())))
        case Some(internalId) ~ enrolments ~ Some(Agent)    => agentAuthCheck(request, enrolments, internalId)
        case Some(internalId) ~ enrolments ~ Some(affinity) => getSubscriptionId(request, enrolments, internalId, affinity)
        case _ =>
          logger.warn("Unable to retrieve internal id or affinity group")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  private def getSubscriptionId[A](request: Request[A],
                                   enrolments: Enrolments,
                                   internalId: String,
                                   affinityGroup: AffinityGroup
  ): Future[Either[Result, IdentifierRequest[A]]] = {

    val cbcEnrolment      = "HMRC-CBC-ORG"
    val cbcNonUKEnrolment = "HMRC-CBC-NONUK-ORG"
    val cbcIdentifier     = "cbcId"

    val subscriptionId: Option[String] = for {
      enrolment      <- enrolments.getEnrolment(cbcEnrolment)
      id             <- enrolment.getIdentifier(cbcIdentifier)
      subscriptionId <- if (id.value.nonEmpty) Some(id.value) else None
    } yield subscriptionId

    val nonUKSubscriptionId: Option[String] = for {
      nonUKEnrolment <- enrolments.getEnrolment(cbcNonUKEnrolment)
      id             <- nonUKEnrolment.getIdentifier(cbcIdentifier)
      subscriptionId <- if (id.value.nonEmpty) Some(id.value) else None
    } yield subscriptionId

    if (subscriptionId.isDefined) {
      Future.successful(Right(IdentifierRequest(request, internalId, subscriptionId.get, affinityGroup)))
    } else if (nonUKSubscriptionId.isDefined) {
      Future.successful(Right(IdentifierRequest(request, internalId, nonUKSubscriptionId.get, affinityGroup)))
    } else {
      logger.warn("Unable to retrieve CBC id from Enrolments")
      Future.successful(Left(Redirect(config.registerUrl)))
    }
  }

  private def cbcDelegatedAuthRule(clientId: String): Enrolment =
    Enrolment("HMRC-CBC-ORG")
      .withIdentifier("cbcId", clientId)
      .withDelegatedAuthRule("cbc-auth")

  private def agentAuthCheck[A](request: Request[A], enrolments: Enrolments, internalId: String)(implicit
    executionContext: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Either[Result, IdentifierRequest[A]]] =
    enrolments.getEnrolment("HMRC-AS-AGENT") match {
      case Some(Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier(_, arn)), _, _)) =>
        sessionRepository.get(internalId).flatMap {
          case None =>
            redirectForAgentContactDetails(request, internalId).map(Left(_))
          case Some(userAnswers) =>
            userAnswers.get(AgentClientIdPage) match {
              case None =>
                logger.info(
                  s"IdentifierAction: Agent with HMRC-AS-AGENT Enrolment. No ClientId in UserAnswers in SessionRepository. Redirecting to /agent/client-id. ${request.headers}"
                )
                Future.successful(Left(Redirect(controllers.agent.routes.AgentClientIdController.onPageLoad())))
              case Some(clientId) => // clientId is cbcid
                logger.info(s"IdentifierAction: Attempting Agent authorisation checking with ${cbcDelegatedAuthRule(clientId)}")
                authorised(cbcDelegatedAuthRule(clientId)) {
                  logger.info("IdentifierAction: Agent with HMRC-AS-AGENT Enrolment and Authorised with cbc-auth Delegated Auth Rule")
                  Future.successful(Right(IdentifierRequest(request, internalId, clientId, Agent, Some(arn))))
                } recover {
                  case _: NoActiveSession =>
                    logger.debug("IdentifierAction: Agent does not have an active session, rendering Session Timeout")
                    Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
                  case _: AuthorisationException =>
                    logger.warn("IdentifierAction: Agent does not have delegated authority for Client. Redirecting to /agent/client-not-identified")
                    Left(Redirect(controllers.client.routes.ClientNotIdentifiedController.onPageLoad))
                }
            }
        }
      case None =>
        logger.warn(s"IdentifierAction: Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments. Redirecting to /agent/use-agent-services")
        Future.successful(Left(Redirect(controllers.agent.routes.AgentUseAgentServicesController.onPageLoad)))
    }

  private def redirectForAgentContactDetails[A](request: Request[A], internalId: String)(implicit hc: HeaderCarrier): Future[Result] =
    agentSubscriptionService.getAgentContactDetails(UserAnswers(internalId)) flatMap {
      case Some(agentUserAnswers) if agentUserAnswers.data == Json.obj() =>
        sessionRepository.set(agentUserAnswers).map {
          _ =>
            Redirect(controllers.agent.routes.AgentContactDetailsNeededController.onPageLoad())
        }
      case Some(_) =>
        logger.info(
          s"IdentifierAction: Agent with HMRC-AS-AGENT Enrolment. No UserAnswers in SessionRepository. Redirecting to /agent/client-id. ${request.headers}"
        )
        Future.successful(Redirect(controllers.agent.routes.AgentClientIdController.onPageLoad()))
      case _ => Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
    }
}

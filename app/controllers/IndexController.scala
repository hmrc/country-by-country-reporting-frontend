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

import connectors.FileDetailsConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.{AgentFirstContactNamePage, ContactNamePage, IsMigratedAgentContactUpdatedPage, IsMigratedUserContactUpdatedPage, JourneyInProgressPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.{AgentSubscriptionService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  subscriptionService: SubscriptionService,
  agentSubscriptionService: AgentSubscriptionService,
  fileConnector: FileDetailsConnector,
  view: IndexView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData.apply) async {
    implicit request =>
      setContactDetailsFlag(request.userAnswers.getOrElse(UserAnswers(request.userId))).flatMap {
        ua =>
          if (request.isAgent) {
            checkForAgentContactDetails(request.subscriptionId, ua)
          } else {
            checkForOrgContactDetails(request.subscriptionId, ua)
          }
      }
  }

  private def checkForAgentContactDetails(subscriptionId: String, ua: UserAnswers)(implicit hc: HeaderCarrier, request: RequestHeader): Future[Result] =
    agentSubscriptionService.getAgentContactDetails(ua) flatMap {
      agentContactDetails =>
        subscriptionService.getContactDetails(agentContactDetails.getOrElse(ua), subscriptionId) flatMap {
          clientContactDetails =>
            (agentContactDetails, clientContactDetails) match {
              case (Some(agentUserAnswers), Some(clientUserAnswers)) =>
                val (updatedAgentUA, updatedClientUA) = setContactMigrationFlag(agentUserAnswers, clientUserAnswers)
                sessionRepository.set(updatedAgentUA).flatMap {
                  _ =>
                    sessionRepository.set(updatedClientUA).flatMap {
                      _ =>
                        if (updatedAgentUA.get(AgentFirstContactNamePage).isEmpty) {
                          Future.successful(Redirect(controllers.agent.routes.AgentContactDetailsNeededController.onPageLoad()))
                        } else if (updatedClientUA.get(ContactNamePage).isEmpty) {
                          Future.successful(Redirect(controllers.client.routes.ClientContactDetailsNeededController.onPageLoad()))
                        } else {
                          fileConnector.getAllFileDetails(subscriptionId) map {
                            fileDetails =>
                              Ok(view(fileDetails.isDefined, subscriptionId, isAgent = true))
                          }
                        }
                    }
                }
              case _ =>
                Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
            }
        }
    }

  private def setContactMigrationFlag(agentUA: UserAnswers, clientUA: UserAnswers): (UserAnswers, UserAnswers) =
    (setAgentMigrationFlag(agentUA), setClientMigrationFlag(clientUA))

  private def setAgentMigrationFlag(agentUserAnswers: UserAnswers): UserAnswers =
    if (agentUserAnswers.get(AgentFirstContactNamePage).isEmpty) {
      agentUserAnswers.set(IsMigratedAgentContactUpdatedPage, false).get
    } else {
      agentUserAnswers
    }

  private def setClientMigrationFlag(clientUserAnswers: UserAnswers): UserAnswers =
    if (clientUserAnswers.get(ContactNamePage).isEmpty) {
      clientUserAnswers.set(IsMigratedUserContactUpdatedPage, false).get
    } else {
      clientUserAnswers
    }

  private def checkForOrgContactDetails(subscriptionId: String, ua: UserAnswers)(implicit hc: HeaderCarrier, request: RequestHeader): Future[Result] =
    subscriptionService.getContactDetails(ua, subscriptionId) flatMap {
      case Some(userAnswers) =>
        sessionRepository.set(userAnswers) flatMap {
          _ =>
            if (userAnswers.get(ContactNamePage).isEmpty) {
              Future.successful(Redirect(routes.ContactDetailsNeededController.onPageLoad()))
            } else {
              fileConnector.getAllFileDetails(subscriptionId) map {
                fileDetails =>
                  Ok(view(fileDetails.isDefined, subscriptionId, isAgent = false))
              }
            }
        }
      case _ =>
        Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
    }

  private def setContactDetailsFlag(userAnswers: UserAnswers): Future[UserAnswers] =
    Future.fromTry(userAnswers.set(JourneyInProgressPage, true)).flatMap {
      ua =>
        sessionRepository.set(ua).map {
          _ => ua
        }
    }

}

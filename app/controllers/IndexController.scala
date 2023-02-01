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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.{AgentFirstContactNamePage, ContactNamePage, JourneyInProgressPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AgentSubscriptionService, SubscriptionService}
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
      Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(JourneyInProgressPage, true)).flatMap {
        ua =>
          sessionRepository.set(ua).flatMap {
            _ =>
              if (request.isAgent) {
                agentSubscriptionService.getAgentContactDetails(ua) flatMap {
                  agentContactDetails =>
                    subscriptionService.getContactDetails(agentContactDetails.getOrElse(ua), request.subscriptionId) flatMap {
                      clientContactDetails =>
                        (agentContactDetails, clientContactDetails) match {
                          case (Some(agentUserAnswers), Some(clientUserAnswers)) =>
                            sessionRepository.set(agentUserAnswers).flatMap {
                              _ =>
                                sessionRepository.set(clientUserAnswers).flatMap {
                                  _ =>
                                    if (agentUserAnswers.get(AgentFirstContactNamePage).isEmpty) {
                                      Future.successful(Redirect(controllers.agent.routes.AgentContactDetailsNeededController.onPageLoad()))
                                    } else if (clientUserAnswers.get(ContactNamePage).isEmpty) {
                                      Future.successful(Redirect(controllers.client.routes.ClientContactDetailsNeededController.onPageLoad()))
                                    } else {
                                      fileConnector.getAllFileDetails(request.subscriptionId) map {
                                        fileDetails =>
                                          Ok(view(fileDetails.isDefined, request.subscriptionId, request.isAgent))
                                      }
                                    }
                                }
                            }
                          case _ =>
                            Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
                        }
                    }
                }
              } else {
                subscriptionService.getContactDetails(ua, request.subscriptionId) flatMap {
                  case Some(userAnswers) =>
                    sessionRepository.set(userAnswers) flatMap {
                      _ =>
                        if (userAnswers.get(ContactNamePage).isEmpty) {
                          Future.successful(Redirect(routes.ContactDetailsNeededController.onPageLoad()))
                        } else {
                          fileConnector.getAllFileDetails(request.subscriptionId) map {
                            fileDetails =>
                              Ok(view(fileDetails.isDefined, request.subscriptionId, request.isAgent))
                          }
                        }
                    }
                  case _ =>
                    Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
                }
              }
          }
      }
  }
}

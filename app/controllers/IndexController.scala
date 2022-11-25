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

import connectors.FileDetailsConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.ContactNamePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AgentSubscriptionService, SubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  subscriptionService: SubscriptionService,
  agentSubscriptionService: AgentSubscriptionService,
  fileConnector: FileDetailsConnector,
  view: IndexView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData.apply) async {
    implicit request =>
      if (request.isAgent) {
        agentSubscriptionService.getAgentContactDetails(request.userAnswers.getOrElse(UserAnswers(request.userId))) flatMap {
          agentContactDetails =>
            subscriptionService.getContactDetails(agentContactDetails.getOrElse(UserAnswers(request.userId)), request.subscriptionId) flatMap {
              clientContactDetails =>
                (agentContactDetails, clientContactDetails) match {
                  case (Some(agentUserAnswers), Some(clientUserAnswers)) =>
                    sessionRepository.set(agentUserAnswers).flatMap {
                      _ =>
                        sessionRepository.set(clientUserAnswers).flatMap {
                          _ =>
                            if (agentUserAnswers.data == Json.obj()) {
                              Future.successful(Redirect(controllers.agent.routes.AgentContactDetailsNeededController.onPageLoad()))
                            } else if (clientUserAnswers.get(ContactNamePage).isEmpty) {
                              Future.successful(Redirect(routes.ContactDetailsNeededController.onPageLoad()))
                            } else {
                              fileConnector.getAllFileDetails(request.subscriptionId) map {
                                fileDetails =>
                                  Ok(view(fileDetails.isDefined, request.subscriptionId))
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
        subscriptionService.getContactDetails(request.userAnswers.getOrElse(UserAnswers(request.userId)), request.subscriptionId) flatMap {
          case Some(userAnswers) =>
            sessionRepository.set(userAnswers) flatMap {
              _ =>
                if (userAnswers.data == Json.obj()) {
                  Future.successful(Redirect(routes.ContactDetailsNeededController.onPageLoad()))
                } else {
                  fileConnector.getAllFileDetails(request.subscriptionId) map {
                    fileDetails =>
                      Ok(view(fileDetails.isDefined, request.subscriptionId))
                  }
                }
            }
          case _ =>
            Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
        }
      }
  }
}

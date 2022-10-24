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

package controllers.agent

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AgentSubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AgentCheckYourAnswersHelper
import viewmodels.govuk.summarylist._
import views.html.ThereIsAProblemView
import views.html.agent.ChangeAgentContactDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ChangeAgentContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  agentSubscriptionService: AgentSubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeAgentContactDetailsView,
  errorView: ThereIsAProblemView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val checkUserAnswersHelper = AgentCheckYourAnswersHelper(request.userAnswers)

      val agentPrimaryContactList = SummaryListViewModel(
        rows = checkUserAnswersHelper.getAgentPrimaryContactDetails
      )

      val agentSecondaryContactList = SummaryListViewModel(
        rows = checkUserAnswersHelper.getAgentSecondaryContactDetails
      )

      agentSubscriptionService.isAgentContactInformationUpdated(request.userAnswers) flatMap {
        case Some(hasContactDetailsChanged) =>
          agentSubscriptionService.doAgentContactDetailsExist map {
            case Some(doContactDetailsExist) =>
              Ok(view(agentPrimaryContactList, agentSecondaryContactList, hasContactDetailsChanged, doContactDetailsExist))
            case _ => InternalServerError(errorView())
          }
        case _ => Future.successful(InternalServerError(errorView()))
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      agentSubscriptionService.doAgentContactDetailsExist flatMap {
        case Some(true) =>
          agentSubscriptionService.updateAgentContactDetails(request.userAnswers) map {
            case true => Redirect(routes.AgentContactDetailsUpdatedController.onPageLoad())
            case _    => InternalServerError(errorView())
          }
        case Some(false) =>
          agentSubscriptionService.createAgentContactDetails("ARN12345", request.userAnswers) map { //TODO: get real ARN
            case true => Redirect(routes.AgentContactDetailsSavedController.onPageLoad())
            case _    => InternalServerError(errorView())
          }
        case _ => Future.successful(InternalServerError(errorView()))
      }
  }
}

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

package controllers.agent

import controllers.actions._
import forms.AgentIsThisYourClientFormProvider
import models.{AgentClientDetails, NormalMode, UserAnswers}
import navigation.Navigator
import pages.{AgentClientDetailsPage, AgentIsThisYourClientPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.agent.AgentIsThisYourClientView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentIsThisYourClientController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  subscriptionService: SubscriptionService,
  requireData: DataRequiredAction,
  formProvider: AgentIsThisYourClientFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AgentIsThisYourClientView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AgentIsThisYourClientPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      for {
        _           <- removeClient(request.userAnswers)
        tradingName <- subscriptionService.getTradingName(request.subscriptionId)
      } yield Ok(view(preparedForm, request.subscriptionId, tradingName.getOrElse("")))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData() andThen requireData).async {

    implicit request =>
      subscriptionService.getTradingName(request.subscriptionId).flatMap {
        tradingName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.subscriptionId, tradingName.getOrElse("")))),
              value =>
                for {
                  updatedAnswers       <- Future.fromTry(request.userAnswers.set(AgentIsThisYourClientPage, value))
                  updatedClientDetails <- setClient(value, request.subscriptionId, tradingName, updatedAnswers)
                  _                    <- sessionRepository.set(updatedClientDetails)
                } yield Redirect(navigator.nextPage(AgentIsThisYourClientPage, NormalMode, updatedAnswers))
            )
      }
  }

  private def setClient(isThisYourClient: Boolean, subscriptionId: String, tradingName: Option[String], userAnswers: UserAnswers): Future[UserAnswers] =
    if (isThisYourClient) {
      Future.fromTry(userAnswers.set(AgentClientDetailsPage, AgentClientDetails(subscriptionId, tradingName)))
    } else { Future.successful(userAnswers) }

  private def removeClient(userAnswers: UserAnswers): Future[UserAnswers] =
    Future.fromTry(userAnswers.remove(AgentClientDetailsPage))

}

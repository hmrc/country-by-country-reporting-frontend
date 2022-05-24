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

import config.FrontendAppConfig
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourAnswersHelper
import viewmodels.govuk.summarylist._
import views.html.{ChangeContactDetailsView, IndexView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class ChangeContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  frontendAppConfig: FrontendAppConfig,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeContactDetailsView,
  errorView: IndexView //TODO: Change to ThereIsAProblemView when implemented
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData.apply andThen requireData).async {
    implicit request =>
      val checkUserAnswersHelper = CheckYourAnswersHelper(request.userAnswers)

      val primaryContactList = SummaryListViewModel(
        rows = checkUserAnswersHelper.getPrimaryContactDetails
      )

      val secondaryContactList = SummaryListViewModel(
        rows = checkUserAnswersHelper.getSecondaryContactDetails
      )

      subscriptionService.isContactInformationUpdated(request.userAnswers) map {
        case Some(hasChanged) => Ok(view(primaryContactList, secondaryContactList, frontendAppConfig, hasChanged))
        case _                => InternalServerError(errorView())
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      subscriptionService.updateContactDetails(request.userAnswers) map {
        case true  => Redirect(routes.DetailsUpdatedController.onPageLoad())
        case false => InternalServerError(errorView())
      }
  }
}

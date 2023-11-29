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

import controllers.actions._
import models.ValidatedFileData
import models.requests.DataRequest
import pages.ValidXMLPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckYourFileDetailsViewModel
import viewmodels.govuk.summarylist._
import views.html.CheckYourFileDetailsView

import javax.inject.Inject

class CheckYourFileDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourFileDetailsView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      request.userAnswers.get(ValidXMLPage) match {
        case Some(details) =>
          val detailsList = SummaryListViewModel(getSummaryRows(details))
            .withoutBorders()
            .withCssClass("govuk-!-margin-bottom-0")
          Ok(view(detailsList))
        case _ =>
          logger.warn("CheckYourFileDetailsController: Unable to retrieve XML information from UserAnswers")
          Redirect(routes.ThereIsAProblemController.onPageLoad())
      }
  }

  private def getSummaryRows(fileDetails: ValidatedFileData)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    if (request.userType == AffinityGroup.Agent) { CheckYourFileDetailsViewModel.getAgentSummaryRows(fileDetails) }
    else { CheckYourFileDetailsViewModel.getSummaryRows(fileDetails) }
}

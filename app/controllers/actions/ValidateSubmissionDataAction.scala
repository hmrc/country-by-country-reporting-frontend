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

package controllers.actions

import controllers.routes
import models.requests.DataRequest
import models.{CheckMode, NormalMode}
import pages.JourneyInProgressPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.CheckYourAnswersValidator

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrgValidationSubmissionDataActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends ValidationSubmissionDataAction {

  override def apply(): ActionRefiner[DataRequest, DataRequest] =
    new OrgValidationSubmissionDataActionProvider()

}

class OrgValidationSubmissionDataActionProvider @Inject() ()(implicit val executionContext: ExecutionContext) extends ActionRefiner[DataRequest, DataRequest] {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    val answers     = request.userAnswers
    val validator   = CheckYourAnswersValidator(answers)
    val currentMode = if (answers.get(JourneyInProgressPage).getOrElse(false)) CheckMode else NormalMode

    validator.changeAnswersRedirectUrl(currentMode, request.isAgent) match {
      case None => Future.successful(Right(request))
      case Some(value) =>
        if (value.equalsIgnoreCase(routes.ChangeContactDetailsController.onPageLoad().url) && !request.isAgent) {
          Future.successful(Right(request))
        } else if (value.equalsIgnoreCase(controllers.client.routes.ChangeClientContactDetailsController.onPageLoad().url) && request.isAgent) {
          Future.successful(Right(request))
        } else {
          Future.successful(Left(Redirect(routes.SomeInformationMissingController.onPageLoad())))
        }
    }
  }
}

trait ValidationSubmissionDataAction {
  def apply(): ActionRefiner[DataRequest, DataRequest]
}

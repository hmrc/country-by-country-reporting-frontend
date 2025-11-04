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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.JourneyInProgressPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.{AgentCheckYourAnswersValidator, CheckYourAnswersValidator}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateMissingContactDataActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends ValidateMissingContactDataAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    val answers = request.userAnswers

    val currentMode = if (answers.get(JourneyInProgressPage).getOrElse(false)) CheckMode else NormalMode

    if (request.isAgent) {
      val agentCheckYourAnswersValidator = AgentCheckYourAnswersValidator(answers)
      agentCheckYourAnswersValidator.changeAnswersRedirectUrl(currentMode) match {
        case Some(_) => Future.successful(Left(Redirect(controllers.agent.routes.AgentSomeInformationMissingController.onPageLoad())))
        case None    => validateNonAgentContactInformation(request, answers, currentMode)
      }
    } else {
      validateNonAgentContactInformation(request, answers, currentMode)
    }
  }

  private def validateNonAgentContactInformation[A](request: DataRequest[A], answers: UserAnswers, currentMode: Mode) = {
    val validator = CheckYourAnswersValidator(answers)
    validator.changeAnswersRedirectUrl(currentMode, request.isAgent) match {
      case Some(_) => Future.successful(Left(Redirect(routes.SomeInformationMissingController.onPageLoad())))
      case None    => Future.successful(Right(request))
    }
  }
}

trait ValidateMissingContactDataAction extends ActionRefiner[DataRequest, DataRequest]

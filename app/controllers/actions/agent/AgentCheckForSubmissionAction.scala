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

package controllers.actions.agent

import controllers.routes
import models.requests.AgentDataRequest
import pages.JourneyInProgressPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentCheckForSubmissionActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends AgentCheckForSubmissionAction {

  override protected def refine[A](request: AgentDataRequest[A]): Future[Either[Result, AgentDataRequest[A]]] =
    if (request.userAnswers.get(JourneyInProgressPage).isEmpty) {
      Future.successful(Left(Redirect(routes.IndexController.onPageLoad)))
    } else {
      Future.successful(Right(request))
    }
}

trait AgentCheckForSubmissionAction extends ActionRefiner[AgentDataRequest, AgentDataRequest]
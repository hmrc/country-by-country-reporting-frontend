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

package controllers.actions.agent

import models.UserAnswers
import models.requests.OptionalAgentDataRequest
import models.requests.agent.AgentIdentifierRequest
import play.api.mvc.ActionTransformer

import scala.concurrent.{ExecutionContext, Future}

class FakeAgentDataRetrievalAction(stubbedUserAnswers: Option[UserAnswers]) extends AgentDataRetrievalAction {

  override def apply(): ActionTransformer[AgentIdentifierRequest, OptionalAgentDataRequest] =
    new FakeAgentDataRetrievalActionProvider(stubbedUserAnswers)
}

class FakeAgentDataRetrievalActionProvider(
  stubbedUserAnswers: Option[UserAnswers] = None
) extends ActionTransformer[AgentIdentifierRequest, OptionalAgentDataRequest] {

  override protected def transform[A](request: AgentIdentifierRequest[A]): Future[OptionalAgentDataRequest[A]] =
    stubbedUserAnswers match {
      case None    => Future(OptionalAgentDataRequest(request, request.userId, None, request.arn))
      case Some(_) => Future(OptionalAgentDataRequest(request, request.userId, stubbedUserAnswers, request.arn))
    }

  implicit override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

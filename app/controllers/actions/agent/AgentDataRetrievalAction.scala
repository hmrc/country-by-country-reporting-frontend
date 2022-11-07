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

package controllers.actions.agent

import controllers.actions.DataRetrievalAction
import models.requests.agent.AgentIdentifierRequest
import models.requests.{IdentifierRequest, OptionalAgentDataRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

class AgentDataRetrievalActionImpl @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends AgentDataRetrievalAction {

  override def apply(): ActionTransformer[AgentIdentifierRequest, OptionalAgentDataRequest] =
    new AgentDataRetrievalActionProvider(sessionRepository)
}

class AgentDataRetrievalActionProvider @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[AgentIdentifierRequest, OptionalAgentDataRequest] {

  override protected def transform[A](request: AgentIdentifierRequest[A]): Future[OptionalAgentDataRequest[A]] =
    sessionRepository.get(request.userId).map {
      OptionalAgentDataRequest(request.request, request.userId, _, request.arn)
    }
}

trait AgentDataRetrievalAction {
  def apply(): ActionTransformer[AgentIdentifierRequest, OptionalAgentDataRequest]
}

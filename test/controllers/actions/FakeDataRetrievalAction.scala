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

package controllers.actions

import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalActionProvider(dataToReturn: Option[UserAnswers]) extends DataRetrievalAction {

  def apply(): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new FakeDataRetrievalAction(dataToReturn)
}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers] = None) extends ActionTransformer[IdentifierRequest, OptionalDataRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    dataToReturn match {
      case None =>
        Future(OptionalDataRequest(request.request, request.userId, None, request.userType, request.subscriptionId))
      case Some(userAnswers) =>
        Future(OptionalDataRequest(request.request, request.userId, Some(userAnswers), request.userType, request.subscriptionId))
    }

  implicit override protected val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

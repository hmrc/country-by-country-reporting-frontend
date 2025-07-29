/*
 * Copyright 2025 HM Revenue & Customs
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

import models.requests.DataRequest
import pages.ContactDetailsJourneyTypePage
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddJourneyNameActionRefinerImpl @Inject()(sessionRepository: SessionRepository)(implicit val executionContext: ExecutionContext)
  extends AddJourneyNameAction {

  override def apply(journeyType: String): ActionRefiner[DataRequest, DataRequest] = new AddJourneyNameActionProvider(journeyType, sessionRepository)
}

class AddJourneyNameActionProvider @Inject()(journeyType: String, sessionrepository: SessionRepository)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[DataRequest, DataRequest] {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    val updatedAnswers = request.userAnswers.set(ContactDetailsJourneyTypePage, journeyType).getOrElse(request.userAnswers)
    sessionrepository
      .set(updatedAnswers)
      .map(
        _ => Right(DataRequest(request.request, request.userId, request.subscriptionId, request.userType, updatedAnswers, request.arn))
      )
  }
}

trait AddJourneyNameAction {
  def apply(journey: String): ActionRefiner[DataRequest, DataRequest]
}

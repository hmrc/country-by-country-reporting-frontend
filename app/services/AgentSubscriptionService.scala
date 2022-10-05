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

package services

import models.UserAnswers
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSubscriptionService @Inject() ()(implicit ec: ExecutionContext) extends Logging {

  //TODO: replace with API call to update agent contact details when agent contact details API's are set up
  def updateAgentContactDetails(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] =
    Future.successful(true)

  //TODO: return if agent contact details exist or not from the API when agent contact details API's are set up
  def doAgentContactDetailsExist(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    Future.successful(Some(false))

  //TODO: return if agent contact details in userAnswers are updated from those from API call when agent contact details API's are set up
  def isAgentContactInformationUpdated(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    Future.successful(Some(true))

}

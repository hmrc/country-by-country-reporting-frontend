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

package connectors

import config.FrontendAppConfig
import models.agentSubscription.{AgentRequestDetailForUpdate, AgentResponseDetail, CreateAgentSubscriptionRequest}
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) extends Logging {

  def createSubscription(
    createAgentSubscriptionRequest: CreateAgentSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {

    val submissionUrl = s"${config.cbcUrl}/country-by-country-reporting/agent/subscription/create-subscription"
    http
      .POST[CreateAgentSubscriptionRequest, HttpResponse](
        submissionUrl,
        createAgentSubscriptionRequest
      )
      .map {
        case response if is2xx(response.status) =>
          Option(response.json)
        case response =>
          logger.warn(s"Unable to create an agent subscription to ETMP. ${response.status} response status")
          None
      }
      .recover {
        case e: Exception =>
          logger.warn(s"Error message ${e.getMessage} has been thrown when create agent subscription was called")
          None
      }
  }

  def checkSubscriptionExists()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Boolean]] = {

    val url = s"${config.cbcUrl}/country-by-country-reporting/agent/subscription/read-subscription"
    http
      .POSTEmpty(url)
      .map {
        case responseMessage if is2xx(responseMessage.status) =>
          Some(true)
        case responseMessage if responseMessage.status == NOT_FOUND =>
          Some(false)
        case otherStatus =>
          logger.warn(s"checkSubscriptionExists: Status $otherStatus has been thrown when read agent subscription was called")
          None
      }
      .recover {
        case e: Exception =>
          logger.warn(s"checkSubscriptionExists: S${e.getMessage} has been thrown when read agent subscription was called")
          None
      }
  }

  def readSubscription()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AgentResponseDetail]] = {

    val url = s"${config.cbcUrl}/country-by-country-reporting/agent/subscription/read-subscription"
    http
      .POSTEmpty(url)
      .map {
        case responseMessage if is2xx(responseMessage.status) =>
          responseMessage.json
            .asOpt[AgentResponseDetail]
        case otherStatus =>
          logger.warn(s"readSubscription: Status $otherStatus has been thrown when read agent subscription was called")
          None
      }
      .recover {
        case e: Exception =>
          logger.warn(s"readSubscription: S${e.getMessage} has been thrown when read agent subscription was called")
          None
      }
  }

  def updateSubscription(requestDetail: AgentRequestDetailForUpdate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = s"${config.cbcUrl}/country-by-country-reporting/agent/subscription/update-subscription"
    http
      .POST[AgentRequestDetailForUpdate, HttpResponse](url, requestDetail)
      .map {
        responseMessage =>
          logger.warn(s"updateSubscription: Status ${responseMessage.status} has been received when update agent subscription was called")
          is2xx(responseMessage.status)
      }
  }

}

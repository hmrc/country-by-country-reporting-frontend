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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  def createSubscription(
    createAgentSubscriptionRequest: CreateAgentSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {

    val submissionUrl = url"${config.cbcUrl}/country-by-country-reporting/agent/subscription/create-subscription"
    http
      .post(submissionUrl)
      .withBody(Json.toJson(createAgentSubscriptionRequest))
      .execute[HttpResponse]
      .map {
        case response if is2xx(response.status) =>
          Option(response.json)
        case response =>
          logger.warn(s"Unable to create an agent subscription to ETMP. ${response.status} response status")
          None
      }
      .recover {
        case e: Exception =>
          logger.error(s"An Error  has been thrown when create agent subscription was called", e)
          None
      }
  }

  def checkSubscriptionExists()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Boolean]] = {

    val url = url"${config.cbcUrl}/country-by-country-reporting/agent/subscription/read-subscription"
    http
      .post(url)
      .execute[HttpResponse]
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
          logger.error(s"checkSubscriptionExists: An error has been thrown when read agent subscription was called", e)
          None
      }
  }

  def readSubscription()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[AgentResponseDetail]] = {

    val url = url"${config.cbcUrl}/country-by-country-reporting/agent/subscription/read-subscription"
    http
      .post(url)
      .execute[HttpResponse]
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
          logger.error(s"readSubscription: An error has been thrown when read agent subscription was called", e)
          None
      }
  }

  def updateSubscription(requestDetail: AgentRequestDetailForUpdate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = url"${config.cbcUrl}/country-by-country-reporting/agent/subscription/update-subscription"
    http
      .post(url)
      .withBody(Json.toJson(requestDetail))
      .execute[HttpResponse]
      .map {
        responseMessage =>
          logger.warn(s"updateSubscription: Status ${responseMessage.status} has been received when update agent subscription was called")
          is2xx(responseMessage.status)
      }
  }

}

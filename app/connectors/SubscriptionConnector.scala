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
import models.subscription.{RequestDetailForUpdate, ResponseDetail}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  def readSubscription(subscriptionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ResponseDetail]] = {

    val url = url"${config.cbcUrl}/country-by-country-reporting/subscription/read-subscription/$subscriptionId"
    http
      .post(url)
      .execute[HttpResponse]
      .map {
        case responseMessage if is2xx(responseMessage.status) =>
          responseMessage.json
            .asOpt[ResponseDetail]
        case otherStatus =>
          logger.warn(s"readSubscription: Status $otherStatus has been thrown when display subscription was called")
          None
      }
      .recover {
        case e: Exception =>
          logger.warn(s"readSubscription: S${e.getMessage} has been thrown when display subscription was called")
          None
      }
  }

  def updateSubscription(requestDetail: RequestDetailForUpdate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = url"${config.cbcUrl}/country-by-country-reporting/subscription/update-subscription"
    http
      .post(url)
      .withBody(Json.toJson(requestDetail))
      .execute[HttpResponse]
      .map {
        responseMessage =>
          logger.warn(s"updateSubscription: Status ${responseMessage.status} has been received when update subscription was called")
          is2xx(responseMessage.status)
      }
  }

}

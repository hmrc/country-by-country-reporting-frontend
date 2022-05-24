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

package connectors

import config.FrontendAppConfig
import models.subscription.{RequestDetailForUpdate, ResponseDetail}
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) extends Logging {

  def readSubscription()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ResponseDetail]] = {

    val url = s"${config.cbcUrl}/country-by-country-reporting/subscription/read-subscription"
    http
      .POSTEmpty(url)
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

    val url = s"${config.cbcUrl}/country-by-country-reporting/subscription/update-subscription"
    http
      .POST[RequestDetailForUpdate, HttpResponse](url, requestDetail)
      .map {
        responseMessage =>
          logger.warn(s"updateSubscription: Status ${responseMessage.status} has been received when update subscription was called")
          is2xx(responseMessage.status)
      }
  }

}

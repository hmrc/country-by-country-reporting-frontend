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
import models.ConversationId
import models.submission.SubmissionDetails
import models.upscan.Reference
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionConnector @Inject() (httpClient: HttpClientV2, config: FrontendAppConfig) extends Logging {

  val submitUrl = url"${config.cbcUrl}/country-by-country-reporting/submit"

  def submitDocument(submissionDetails: SubmissionDetails)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[ConversationId]] =
    httpClient
      .post(submitUrl)
      .withBody(Json.toJson(submissionDetails))
      .setHeader("x-file-reference-id" -> submissionDetails.fileReference.value)
      .execute[HttpResponse] map {
      case response if is2xx(response.status) => Option(response.json.as[ConversationId])
      case errorResponse =>
        logger.warn(s"Failed to submit document with upload Id [${submissionDetails.uploadId.value}]: received status: ${errorResponse.status}")
        None
    }
}

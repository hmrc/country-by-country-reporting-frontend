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
import models.upscan._
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject() (configuration: FrontendAppConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def getUpscanFormData(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[UpscanInitiateResponse] = {
    val callbackUrl = s"$backendUrl/callback"
    val body = UpscanInitiateRequest(
      callbackUrl,
      Some(upscanRedirectBase + controllers.routes.UploadFileController.getStatus(uploadId).url),
      Some(s"$upscanRedirectBase/send-a-country-by-country-report/error"),
      None,
      Some(upscanMaxSize * 1048576),
      Some("text/xml")
    )
    httpClient
      .post(url"$upscanInitiateUrl")
      .withBody(Json.toJson(body))
      .setHeader(headers.toSeq :+ ("X-Upload-Id" -> uploadId.value): _*)
      .execute[PreparedUpload] map {
      _.toUpscanInitiateResponse
    }
  }

  def requestUpload(uploadId: UploadId, fileReference: Reference)(implicit hc: HeaderCarrier): Future[UploadId] = {
    val uploadUrl = url"$backendUrl/upscan/upload"
    httpClient.post(uploadUrl).withBody(Json.toJson(UpscanIdentifiers(uploadId, fileReference))).execute[http.HttpResponse] map {
      _ => uploadId
    }
  }

  def getUploadDetails(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[Option[UploadSessionDetails]] = {
    val detailsUrl = url"$backendUrl/upscan/details/${uploadId.value}"
    httpClient.get(detailsUrl).execute[http.HttpResponse] map {
      response =>
        response.status match {
          case status if is2xx(status) =>
            response.json.validate[UploadSessionDetails] match {
              case JsSuccess(details, _) => Some(details)
              case JsError(_) =>
                logger.warn(s"GetUploadDetails: not a valid json")
                None
            }
          case _ =>
            logger.warn(s"Failed to getUploadDetails")
            None
        }
    }
  }

  def getUploadStatus(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[Option[UploadStatus]] = {
    val statusUrl = url"$backendUrl/upscan/status/${uploadId.value}"
    httpClient.get(statusUrl).execute[HttpResponse] map {
      response =>
        response.status match {
          case OK =>
            response.json.validate[UploadStatus] match {
              case JsSuccess(status, _) =>
                Some(status)
              case JsError(_) =>
                logger.warn(s"GetUploadStatus: not a valid json")
                None
            }
          case _ =>
            logger.warn(s"Failed to getUploadStatus")
            None
        }
    }
  }

  private[connectors] val upscanInitiatePath: String = "/upscan/v2/initiate"
  private val backendUrl                             = s"${configuration.cbcUrl}/country-by-country-reporting"
  private val upscanInitiateUrl                      = s"${configuration.upscanInitiateHost}$upscanInitiatePath"
  private val upscanRedirectBase                     = configuration.upscanRedirectBase
  private val upscanMaxSize                          = configuration.upscanMaxFileSize
}

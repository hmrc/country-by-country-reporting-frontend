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

package helpers

import config.FrontendAppConfig
import connectors.UpscanConnector
import models.upscan._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeUpscanConnector @Inject() (configuration: FrontendAppConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
    extends UpscanConnector(configuration, httpClient) {

  var statusBuffer: Option[UploadStatus]          = None
  var detailsBuffer: Option[UploadSessionDetails] = None

  def setStatus(uploadStatus: UploadStatus): Unit =
    statusBuffer = Some(uploadStatus)

  def setDetails(uploadDetails: UploadSessionDetails): Unit =
    detailsBuffer = Some(uploadDetails)

  def resetDetails(): Unit =
    detailsBuffer = None

  override def getUpscanFormData(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[UpscanInitiateResponse] =
    Future.successful(
      UpscanInitiateResponse(
        fileReference = Reference("file-reference"),
        postTarget = "target",
        formFields = Map.empty
      )
    )

  override def requestUpload(uploadId: UploadId, fileReference: Reference)(implicit hc: HeaderCarrier): Future[UploadId] =
    Future.successful(uploadId)

  override def getUploadStatus(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[Option[UploadStatus]] =
    Future.successful(statusBuffer)

  override def getUploadDetails(uploadId: UploadId)(implicit hc: HeaderCarrier): Future[Option[UploadSessionDetails]] =
    Future.successful(detailsBuffer)

}

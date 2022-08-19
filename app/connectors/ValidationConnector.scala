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
import models.upscan.UpscanURL
import models.{Errors, InvalidXmlError, MessageSpecData, NonFatalErrors, SubmissionValidationFailure, SubmissionValidationResult, SubmissionValidationSuccess}
import play.api.Logging
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ValidationConnector @Inject() (http: HttpClient, config: FrontendAppConfig) extends Logging {

  val url = s"${config.cbcUrl}/country-by-country-reporting/validate-submission"

  def sendForValidation(upScanUrl: UpscanURL)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Errors, MessageSpecData]] =
    http
      .POST[UpscanURL, HttpResponse](url, upScanUrl)
      .map {
        response =>
          response.status match {
            case OK =>
              response.json.as[SubmissionValidationResult] match {
                case x: SubmissionValidationSuccess =>
                  Right(x.messageSpecData)
                case x: SubmissionValidationFailure =>
                  Left(x.validationErrors)
              }
          }
      }
      .recover {
        case NonFatal(e) =>
          if (e.getMessage contains "Invalid XML") {
            logger.warn(s"XML parsing failed. The XML parser in mandatory-disclosure-rules backend has thrown the exception: $e")
            Left(InvalidXmlError(e.getMessage))
          } else {
            logger.warn(s"Remote service timed out. The XML parser in mandatory-disclosure-rules backend backend has thrown the exception: $e")
            Left(NonFatalErrors(e.getMessage))
          }
      }
}

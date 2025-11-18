/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.testOnlyDoNotUseInAppConf

import config.FrontendAppConfig
import models.ConversationId
import play.api.Logging
import play.api.http.HeaderNames
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq}
import play.api.libs.ws.DefaultBodyWritables._

class TestSubmissionConnector @Inject() (httpClient: HttpClientV2, config: FrontendAppConfig) extends Logging {

  val submitXmlUrl = url"${config.cbcUrl}/country-by-country-reporting/submit-xml"

  def submitXmlDocument(fileName: String, enrolmentID: String, xmlDocument: NodeSeq)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[ConversationId]] = {
    val body = constructSubmission(fileName, enrolmentID, xmlDocument).toString()
    httpClient.post(submitXmlUrl).withBody(body).setHeader(headers: _*).execute[HttpResponse] map {
      case response if is2xx(response.status) => Some(response.json.as[ConversationId])
      case errorResponse =>
        logger.warn(s"Failed to submit  XML document: received status: ${errorResponse.status} and message: ${errorResponse.body}")
        None
    }
  }

  private def constructSubmission(fileName: String, enrolmentID: String, document: NodeSeq): NodeSeq = {
    val submission =
      <submission>
        <fileName>{fileName}</fileName>
        <enrolmentID>{enrolmentID}</enrolmentID>
        <file></file>
      </submission>

    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case elem: Elem if elem.label == "file" =>
          elem.copy(child = document)
        case other => other
      }
    }).transform(submission).head
  }

  private val headers = Seq(
    HeaderNames.CONTENT_TYPE -> "application/xml"
  )

}

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

package models.agentSubscription

import play.api.libs.json.{Json, OFormat}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class AgentRequestParameters(paramName: String, paramValue: String)

object AgentRequestParameters {
  implicit val formats: OFormat[AgentRequestParameters] = Json.format[AgentRequestParameters]
}

case class AgentRequestCommonForSubscription(
  regime: String,
  conversationID: Option[String] = None,
  receiptDate: String,
  acknowledgementReference: String,
  originatingSystem: String,
  requestParameters: Option[Seq[AgentRequestParameters]]
)

object AgentRequestCommonForSubscription {

  implicit val requestCommonForSubscriptionFormats: OFormat[AgentRequestCommonForSubscription] =
    Json.format[AgentRequestCommonForSubscription]

  private val mdtp = "MDTP"

  def createAgentRequestCommonForSubscription(): AgentRequestCommonForSubscription = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    //Generate a 32 chars UUID without hyphens
    val acknowledgementReference = UUID.randomUUID().toString.replace("-", "")

    AgentRequestCommonForSubscription(
      regime = "CBC",
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = acknowledgementReference,
      originatingSystem = mdtp,
      requestParameters = None
    )
  }

}

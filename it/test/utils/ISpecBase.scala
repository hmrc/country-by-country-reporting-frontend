/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.Json

trait ISpecBase extends WireMockMethods {

  private val authoriseUri = "/auth/authorise"

  val enrolmentKey         = "HMRC-CBC-ORG"
  val organisationAffinity = "Organisation"
  val cbcIdentifier        = "cbcId"
  val cbcIdValue           = "XACBC0000123777"

  //  val agentEnrolmentKey = "HMRC-AS-AGENT"
  //  val agentAffinity = "Agent"
  //  val agentIdentifier = "AgentReferenceNumber"
  //  val arnValue = "ARN12377B"

  val groupId        = "00000000-0000-0000-0000-000000000666"
  val credentialRole = "User"
  val internalId     = "1234"
  val credId         = "1234"

  def authorised(): StubMapping =
    when(method = POST, uri = authoriseUri)
      .thenReturn(
        status = OK,
        body = Json.obj(
          "affinityGroup" -> organisationAffinity,
          "allEnrolments" -> Json.arr(
            Json.obj(
              "key" -> enrolmentKey,
              "identifiers" -> Json.arr(
                Json.obj(
                  "key"   -> cbcIdentifier,
                  "value" -> cbcIdValue,
                  "state" -> "activated"
                )
              )
            )
          ),
          "internalId"      -> internalId,
          "groupIdentifier" -> groupId,
          "credentialRole"  -> credentialRole,
          "optionalCredentials" -> Json.obj(
            "providerId"   -> credId,
            "providerType" -> "credType"
          )
        )
      )

  def unauthorised(): StubMapping =
    when(method = POST, uri = authoriseUri).thenReturn(status = UNAUTHORIZED)
}

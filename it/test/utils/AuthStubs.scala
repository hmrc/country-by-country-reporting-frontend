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

import org.scalatest.Suite
import play.api.http.Status.OK

trait AuthStubs { this: Suite =>

  val authUrl                = "/auth/authorise"
  val problemClientAccessUrl = "/agent/problem/client-access"
  val testAuthInternalId     = "internalId"

  val authRequest: String =
    s"""
       |{
       |  "authorise": [
       |    {
       |      "authProviders": [ "GovernmentGateway" ]
       |    },
       |    {
       |      "confidenceLevel": 50
       |    }
       |  ],
       |  "retrieve": [ "internalId", "allEnrolments", "affinityGroup" ]
       |}
       |""".stripMargin

  def authOKResponse(cbcId: String): String =
    s"""|  {
        |    "internalId": "$testAuthInternalId",
        |    "affinityGroup": "Organisation",
        |    "allEnrolments" : [ {
        |      "key" : "HMRC-CBC-ORG",
        |      "identifiers" : [ {
        |        "key" : "cbcId",
        |        "value" : "$cbcId"
        |      } ],
        |      "state" : "Activated",
        |      "confidenceLevel" : 50
        |    } ]
        |  }
         """.stripMargin

  def authOkResponseForAgent(): String =
    s"""
       |{
       |  "internalId" : "$testAuthInternalId",
       |  "allEnrolments" : [ {
       |    "key" : "HMRC-AS-AGENT",
       |    "identifiers" : [ {
       |      "key" : "AgentReferenceNumber",
       |      "value" : "testARN"
       |    } ],
       |    "state" : "Activated",
       |    "confidenceLevel" : 50
       |  } ],
       |  "affinityGroup" : "Agent"
       |}
       |""".stripMargin
}

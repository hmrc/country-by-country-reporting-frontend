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

trait AuthStubs extends WireMockHelper { this: Suite =>

  val authUrl            = "/auth/authorise"
  val testAuthInternalId = "internalId"

  val authRequest =
    s"""{
       |  "authorise":[
       |    [
       |      {
       |         "authProviders":["GovernmentGateway"]
       |      },
       |      {
       |         "identifiers":[],
       |         "state":"Activated",
       |         "enrolment":"HMRC-CBC-ORG"
       |      },
       |      {
       |        "credentialStrength":"strong"
       |      }
       |    ],
       |  {
       |    "affinityGroup":"Organisation"
       |  },
       |  {
       |    "confidenceLevel":50
       |  }],
       |  "retrieve":[ "internalId", "affinityGroup", "allEnrolments" ]
       |}""".stripMargin

  def authOKResponse(cbcId: String) =
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

  //get internalId object and affinityGroup object and allEnrolments ^^
  def stubAuthorised(appaId: String): Unit =
    stubPost(authUrl, OK, authRequest, authOKResponse(appaId))

  def verifyAuthorised(): Unit =
    verifyPost(authUrl)
}

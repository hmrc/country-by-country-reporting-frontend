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

import generators.ModelGenerators
import models.SubscriptionID
import models.agentSubscription.{AgentRequestDetailForUpdate, AgentResponseDetail, CreateAgentSubscriptionRequest}
import org.scalacheck.Arbitrary
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentSubscriptionConnectorSpec extends Connector with ModelGenerators {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.country-by-country-reporting.port" -> server.port()
    )
    .build()

  lazy val connector: AgentSubscriptionConnector = app.injector.instanceOf[AgentSubscriptionConnector]
  private val createSubscriptionUrl              = "/country-by-country-reporting/agent/subscription/create-subscription"
  private val readSubscriptionUrl                = "/country-by-country-reporting/agent/subscription/read-subscription"
  private val updateSubscriptionUrl              = "/country-by-country-reporting/agent/subscription/update-subscription"

  val agentResponseDetailString: String =
    """
      |{
      |"subscriptionID": "111111111",
      |"tradingName": "",
      |"isGBUser": true,
      |"primaryContact":
      |{
      |"email": "",
      |"phone": "",
      |"mobile": "",
      |"organisation": {
      |"organisationName": "orgName"
      |}
      |},
      |"secondaryContact":
      |{
      |"email": "",
      |"organisation": {
      |"organisationName": ""
      |}
      |}
      |}""".stripMargin

  val agentResponseDetail: AgentResponseDetail = Json.parse(agentResponseDetailString).as[AgentResponseDetail]

  "AgentSubscriptionConnector" - {
    "createSubscription" - {
      val createAgentSubscriptionRequest = Arbitrary.arbitrary[CreateAgentSubscriptionRequest].sample.value

      "must return SubscriptionID for valid input request" in {
        val expectedResponse = SubscriptionID("XACBC0000123456")

        val subscriptionResponse: String =
          s"""
             |{
             | "createAgentSubscriptionForCBCResponse": {
             |"responseCommon": {
             |"status": "OK",
             |"processingDate": "1000-01-01T00:00:00Z"
             |  },
             |  "responseDetail": {
             |   "subscriptionID": "XACBC0000123456"
             |  }
             |} }""".stripMargin

        stubPostResponse(createSubscriptionUrl, OK, subscriptionResponse)

        val result: Future[Option[SubscriptionID]] = connector.createSubscription(createAgentSubscriptionRequest)
        result.futureValue.value mustBe expectedResponse
      }

      "must return None for invalid json response" in {
        val subscriptionResponse: String =
          s"""
             |{
             | "createAgentSubscriptionForCBCResponse": {
             |"responseCommon": {
             |"status": "OK",
             |"processingDate": "1000-01-01T00:00:00Z"
             |  },
             |  "responseDetail": {
             |  }
             |} }""".stripMargin

        stubPostResponse(createSubscriptionUrl, OK, subscriptionResponse)

        val result = connector.createSubscription(createAgentSubscriptionRequest)
        result.futureValue mustBe None
      }

      "must return None when create subscription fails" in {
        val errorCode: Int = errorCodes.sample.value

        val subscriptionErrorResponse: String =
          s"""
             | "errorDetail": {
             |    "timestamp": "2016-08-16T18:15:41Z",
             |    "correlationId": "f058ebd6-02f7-4d3f-942e-904344e8cde5",
             |    "errorCode": "$errorCode",
             |    "errorMessage": "Internal error",
             |    "source": "Internal error"
             |  }
             |""".stripMargin

        stubPostResponse(createSubscriptionUrl, errorCode, subscriptionErrorResponse)

        val result = connector.createSubscription(createAgentSubscriptionRequest)
        result.futureValue mustBe None
      }
    }

    "checkSubscriptionExists" - {
      "must return true when readSubscription is successful" in {
        stubPostResponse(readSubscriptionUrl, OK, agentResponseDetailString)

        whenReady(connector.checkSubscriptionExists()) {
          result =>
            result mustBe Some(true)
        }
      }

      "must return false when readSubscription fails with a NOT_FOUND" in {
        stubPostResponse(readSubscriptionUrl, NOT_FOUND)

        whenReady(connector.checkSubscriptionExists()) {
          result =>
            result mustBe Some(false)
        }
      }

      "must return a None when readSubscription fails with InternalServerError" in {
        stubPostResponse(readSubscriptionUrl, INTERNAL_SERVER_ERROR)

        whenReady(connector.readSubscription()) {
          result =>
            result mustBe None
        }
      }
    }

    "readSubscription" - {
      "must return a AgentResponseDetails when readSubscription is successful" in {
        stubPostResponse(readSubscriptionUrl, OK, agentResponseDetailString)

        whenReady(connector.readSubscription()) {
          result =>
            result mustBe Some(agentResponseDetail)
        }
      }

      "must return a None when readSubscription fails with InternalServerError" in {
        stubPostResponse(readSubscriptionUrl, INTERNAL_SERVER_ERROR)

        whenReady(connector.readSubscription()) {
          result =>
            result mustBe None
        }
      }
    }

    "updateSubscription" - {
      "must return status 200 when updateSubscription is successful" in {
        val agentRequestDetails = Arbitrary.arbitrary[AgentRequestDetailForUpdate].sample.value
        stubPostResponse(updateSubscriptionUrl, OK)

        whenReady(connector.updateSubscription(agentRequestDetails)) {
          result =>
            result mustBe true
        }
      }

      "must return a error status code when updateSubscription fails with Error" in {
        val agentRequestDetails = Arbitrary.arbitrary[AgentRequestDetailForUpdate].sample.value

        val errorCode = errorCodes.sample.value
        stubPostResponse(updateSubscriptionUrl, errorCode)

        whenReady(connector.updateSubscription(agentRequestDetails)) {
          result =>
            result mustBe false
        }
      }
    }
  }

}

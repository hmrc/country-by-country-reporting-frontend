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
import models.agentSubscription.{AgentRequestDetailForUpdate, AgentResponseDetail}
import org.scalacheck.Arbitrary
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class AgentSubscriptionConnectorSpec extends Connector with ModelGenerators {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.country-by-country-reporting.port" -> server.port()
    )
    .build()

  lazy val connector: AgentSubscriptionConnector = app.injector.instanceOf[AgentSubscriptionConnector]
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

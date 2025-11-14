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

package services

import base.SpecBase
import connectors.AgentSubscriptionConnector
import generators.ModelGenerators
import models.UserAnswers
import models.agentSubscription.{AgentContactInformation, AgentDetails, AgentResponseDetail}
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AgentSubscriptionServiceSpec extends SpecBase with ModelGenerators {

  val mockAgentSubscriptionConnector: AgentSubscriptionConnector = mock[AgentSubscriptionConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AgentSubscriptionConnector].toInstance(mockAgentSubscriptionConnector)
    )
    .build()

  val service: AgentSubscriptionService = new AgentSubscriptionService(mockAgentSubscriptionConnector)

  "AgentSubscriptionService" - {
    "CreateAgentContactDetails" - {
      "must return 'true' on successfully creating subscription" in {
        val arn                        = "ARN12345"
        val responseCreateSubscription = Future.successful(Option(Json.obj()))
        when(mockAgentSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(responseCreateSubscription)

        val userAnswers = UserAnswers("")
          .set(AgentFirstContactNamePage, "TestName")
          .success
          .value
          .set(AgentFirstContactEmailPage, "test@gmail.com")
          .success
          .value
          .set(AgentFirstContactPhonePage, "000000000")
          .success
          .value
          .set(AgentHaveSecondContactPage, false)
          .success
          .value

        val result = service.createAgentContactDetails(arn, userAnswers)
        result.futureValue mustBe true

        verify(mockAgentSubscriptionConnector, times(1)).createSubscription(any())(any(), any())
      }

      "must return false when unable to create subscription because UserAnswers is empty" in {
        val responseCreateSubscription: Future[Option[JsValue]] = Future.successful(None)
        val arn                                                 = "ARN12345"

        when(mockAgentSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(responseCreateSubscription)

        val result = service.createAgentContactDetails(arn, UserAnswers("id"))

        result.futureValue mustBe false
      }

      "must return false when it fails to create subscription" in {
        val arn = "ARN12345"
        val userAnswers = UserAnswers("")
          .set(AgentFirstContactNamePage, "TestName")
          .success
          .value
          .set(AgentFirstContactEmailPage, "test@gmail.com")
          .success
          .value
          .set(AgentFirstContactPhonePage, "000000000")
          .success
          .value
          .set(AgentHaveSecondContactPage, false)
          .success
          .value

        val responseCreateSubscription: Future[Option[JsValue]] = Future.successful(None)

        when(mockAgentSubscriptionConnector.createSubscription(any())(any(), any())).thenReturn(responseCreateSubscription)

        val result = service.createAgentContactDetails(arn, userAnswers)

        result.futureValue mustBe false
      }
    }

    "GetAgentContactDetails" - {
      "must call the agent subscription connector and return a UserAnswers populated with returned contact details for Agent" in {
        val responseDetailString: String =
          """
            |{
            |"subscriptionID": "111111111",
            |"tradingName": "",
            |"isGBUser": true,
            |"primaryContact":
            |{
            |"email": "test@test.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "acme"
            |}
            |},
            |"secondaryContact":
            |{
            |"email": "test@test.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "wer"
            |}
            |}
            |}""".stripMargin

        val agentResponseDetail = Json.parse(responseDetailString).as[AgentResponseDetail]

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentResponseDetail)))

        val result = service.getAgentContactDetails(emptyUserAnswers)

        val ua = result.futureValue.value

        ua.get(AgentFirstContactNamePage) mustBe Some("acme")
        ua.get(AgentFirstContactEmailPage) mustBe Some("test@test.com")
        ua.get(AgentFirstContactPhonePage) mustBe Some("99999")
      }

      "must call the agent subscription connector and return a empty user answers for the agent coming into the service for the first time" in {

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(None))

        val result = service.getAgentContactDetails(emptyUserAnswers)

        val ua = result.futureValue.value

        ua.data mustBe Json.obj()
      }
    }

    "updateAgentContactDetails" - {
      "must return true on updating agentContactDetails" in {
        val agentContactDetails = Arbitrary.arbitrary[AgentResponseDetail].sample.value
        val userAnswers = emptyUserAnswers
          .set(AgentFirstContactEmailPage, "test@email.com")
          .success
          .value
          .set(AgentFirstContactHavePhonePage, true)
          .success
          .value
          .set(AgentFirstContactPhonePage, "+4411223344")
          .success
          .value
          .set(AgentHaveSecondContactPage, true)
          .success
          .value
          .set(AgentSecondContactEmailPage, "test1@email.com")
          .success
          .value
          .set(AgentSecondContactHavePhonePage, true)
          .success
          .value
          .set(AgentSecondContactPhonePage, "+3311211212")
          .success
          .value
        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentContactDetails)))
        when(mockAgentSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(true))

        service.updateAgentContactDetails(userAnswers).futureValue mustBe true

      }

      "must return false on failing to update the agentContactDetails" in {
        val agentContactDetails = Arbitrary.arbitrary[AgentResponseDetail].sample.value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentContactDetails)))
        when(mockAgentSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(false))

        service.updateAgentContactDetails(emptyUserAnswers).futureValue mustBe false

      }

      "must return false on failing to get response from readSubscription" in {
        val agentContactDetails = Arbitrary.arbitrary[AgentResponseDetail].sample.value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentContactDetails)))
        when(mockAgentSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(false))

        service.updateAgentContactDetails(emptyUserAnswers).futureValue mustBe false

      }
    }

    "isAgentContactInformationUpdated" - {
      "return false when ReadSubscription data is not changed for agent flow" in {
        val agentResponseDetail = AgentResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = AgentContactInformation(AgentDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(AgentFirstContactNamePage, "orgName")
          .success
          .value
          .set(AgentFirstContactEmailPage, "test@test.com")
          .success
          .value
          .set(AgentFirstContactHavePhonePage, true)
          .success
          .value
          .set(AgentFirstContactPhonePage, "+4411223344")
          .success
          .value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentResponseDetail)))

        val result = service.isAgentContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(false)
      }

      "return true when ReadSubscription data secondaryContact is None and user updated the secondary contact for agent flow" in {
        val agentResponseDetail = AgentResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = AgentContactInformation(AgentDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(AgentFirstContactNamePage, "orgName")
          .success
          .value
          .set(AgentFirstContactEmailPage, "test@test.com")
          .success
          .value
          .set(AgentFirstContactHavePhonePage, true)
          .success
          .value
          .set(AgentFirstContactPhonePage, "+4411223344")
          .success
          .value
          .set(AgentHaveSecondContactPage, true)
          .success
          .value
          .set(AgentSecondContactNamePage, "SecOrgName")
          .success
          .value
          .set(AgentSecondContactEmailPage, "test1@email.com")
          .success
          .value
          .set(AgentSecondContactHavePhonePage, true)
          .success
          .value
          .set(AgentSecondContactPhonePage, "+3311211212")
          .success
          .value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentResponseDetail)))

        val result = service.isAgentContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(true)
      }

      "return true when ReadSubscription data is changed for agent flow" in {
        val agentResponseDetail = AgentResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = AgentContactInformation(AgentDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(AgentFirstContactNamePage, "orgName")
          .success
          .value
          .set(AgentFirstContactEmailPage, "changetest@test.com")
          .success
          .value
          .set(AgentFirstContactHavePhonePage, true)
          .success
          .value
          .set(AgentFirstContactPhonePage, "+4411223344")
          .success
          .value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(agentResponseDetail)))

        val result = service.isAgentContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(true)
      }

      "return None when ReadSubscription fails to return the details" in {

        val userAnswers = emptyUserAnswers
          .set(AgentFirstContactEmailPage, "changetest@test.com")
          .success
          .value
          .set(AgentFirstContactHavePhonePage, true)
          .success
          .value
          .set(AgentFirstContactPhonePage, "+4411223344")
          .success
          .value

        when(mockAgentSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(None))

        val result = service.isAgentContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe None
      }
    }

    "doAgentContactDetailsExist" - {
      "must return true if agentContactDetails exist" in {
        when(mockAgentSubscriptionConnector.checkSubscriptionExists()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(true)))

        val result = service.doAgentContactDetailsExist()

        result.futureValue.value mustBe true
      }

      "must return false if agentContactDetails don't exist" in {

        when(mockAgentSubscriptionConnector.checkSubscriptionExists()(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(false)))

        val result = service.doAgentContactDetailsExist()

        result.futureValue.value mustBe false
      }
    }
  }
}

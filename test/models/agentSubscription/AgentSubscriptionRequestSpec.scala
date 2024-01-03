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

package models.agentSubscription

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.scalatest.Inside.inside
import pages.{
  AgentFirstContactEmailPage,
  AgentFirstContactHavePhonePage,
  AgentFirstContactNamePage,
  AgentFirstContactPhonePage,
  AgentHaveSecondContactPage,
  AgentSecondContactEmailPage,
  AgentSecondContactHavePhonePage,
  AgentSecondContactNamePage,
  AgentSecondContactPhonePage
}

class AgentSubscriptionRequestSpec extends SpecBase {

  private val arn      = "ARN"
  private val arnValue = "ARN123456789"
  private val name     = "name"
  private val email    = "a@b.com"
  private val phone    = "0987654321"
  private val cbc      = "CBC"
  private val mdtp     = "MDTP"

  "createAgentSubscriptionRequest must" - {

    "return a Right(AgentSubscriptionRequest) with one set of contact details if they exist" in {

      val userAnswers = emptyUserAnswers
        .set(AgentFirstContactNamePage, name)
        .success
        .value
        .set(AgentFirstContactEmailPage, email)
        .success
        .value
        .set(AgentFirstContactHavePhonePage, true)
        .success
        .value
        .set(AgentFirstContactPhonePage, phone)
        .success
        .value
        .set(AgentHaveSecondContactPage, false)
        .success
        .value

      val result = AgentSubscriptionRequest.createAgentSubscriptionRequest(arnValue, userAnswers)

      inside(result) {
        case Right(agentSubscriptionRequest) =>
          agentSubscriptionRequest.requestCommon mustBe a[AgentRequestCommonForSubscription]
          agentSubscriptionRequest.requestDetail mustBe
            AgentRequestDetail(arn, arnValue, None, isGBUser = true, AgentContactInformation(AgentDetails(name), email, Some(phone), None), None)
      }
    }

    "return a Right(AgentSubscriptionRequest) with two sets of contact details if they exist" in {

      val userAnswers = emptyUserAnswers
        .set(AgentFirstContactNamePage, name)
        .success
        .value
        .set(AgentFirstContactEmailPage, email)
        .success
        .value
        .set(AgentFirstContactHavePhonePage, true)
        .success
        .value
        .set(AgentFirstContactPhonePage, phone)
        .success
        .value
        .set(AgentHaveSecondContactPage, true)
        .success
        .value
        .set(AgentSecondContactNamePage, name)
        .success
        .value
        .set(AgentSecondContactEmailPage, email)
        .success
        .value
        .set(AgentSecondContactHavePhonePage, true)
        .success
        .value
        .set(AgentSecondContactPhonePage, phone)
        .success
        .value

      val result = AgentSubscriptionRequest.createAgentSubscriptionRequest(arnValue, userAnswers)

      inside(result) {
        case Right(agentSubscriptionRequest) =>
          agentSubscriptionRequest.requestCommon mustBe a[AgentRequestCommonForSubscription]
          agentSubscriptionRequest.requestDetail mustBe
            AgentRequestDetail(
              arn,
              arnValue,
              None,
              isGBUser = true,
              AgentContactInformation(AgentDetails(name), email, Some(phone), None),
              Some(AgentContactInformation(AgentDetails(name), email, Some(phone), None))
            )
      }
    }

  }
}

/*
 * Copyright 2026 HM Revenue & Customs
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
import generators.ModelGenerators
import models.{AgentClientDetails, UserAnswers}
import org.scalacheck.Arbitrary
import pages.{
  AgentClientDetailsPage,
  AgentFirstContactEmailPage,
  AgentFirstContactHavePhonePage,
  AgentFirstContactPhonePage,
  AgentHaveSecondContactPage,
  AgentSecondContactEmailPage,
  AgentSecondContactHavePhonePage,
  AgentSecondContactPhonePage
}

class AgentRequestDetailForUpdateSpec extends SpecBase with ModelGenerators {
  val agentContactDetails: AgentResponseDetail = Arbitrary.arbitrary[AgentResponseDetail].sample.value
  val userAnswers: UserAnswers = emptyUserAnswers
    .withPage(AgentFirstContactEmailPage, "test@email.com")
    .withPage(AgentFirstContactHavePhonePage, true)
    .withPage(AgentFirstContactPhonePage, "+4411223344")
    .withPage(AgentHaveSecondContactPage, true)
    .withPage(AgentSecondContactEmailPage, "test1@email.com")
    .withPage(AgentSecondContactHavePhonePage, true)
    .withPage(AgentSecondContactPhonePage, "+3311211212")
    .withPage(AgentClientDetailsPage, AgentClientDetails("cbcId-123", "Test Business"))

  "AgentRequestDetailForUpdate" - {
    "must convert to AgentRequestDetailForUpdate successfully" in {
      val result = AgentRequestDetailForUpdate.convertToRequestDetails(agentContactDetails, userAnswers).get
      result.IDType mustEqual "ARN"
      result.IDNumber mustEqual agentContactDetails.subscriptionID
      result.tradingName mustEqual agentContactDetails.tradingName
      result.isGBUser mustEqual agentContactDetails.isGBUser
      result.primaryContact.email mustEqual userAnswers.get(AgentFirstContactEmailPage).get
      result.cbcId mustEqual Some("cbcId-123")
      result.agentClient mustEqual Some("Test Business")
    }
  }
}

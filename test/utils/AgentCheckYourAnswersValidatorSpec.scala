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

package utils

import base.SpecBase
import models.CheckMode
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._

class AgentCheckYourAnswersValidatorSpec extends SpecBase {
  "ChangeAnswersRedirectUrl" - {

    "must return ChangeFirstContactName if all mandatory values are not available" in {
      val changeUrl = AgentCheckYourAnswersValidator(emptyUserAnswers).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-first-contact-name")
    }

    "must return ChangeFirstContactEmail if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers.withPage(AgentFirstContactNamePage, "test")
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-first-contact-email")
    }

    "must return FirstContactHavePhone if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers.withPage(AgentFirstContactNamePage, "test").withPage(AgentFirstContactEmailPage, "test@test.com")
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-first-contact-have-phone")
    }

    "must return FirstContactHavePhone if all mandatory values are not available - AgentFirstContactPhonePage" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, true)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-first-contact-have-phone")
    }

    "must return AgentHaveSecondContact if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-have-second-contact")
    }

    "must return AgentSecondContactName if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, true)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-have-second-contact")
    }

    "must return AgentSecondContactEmail if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, true)
        .withPage(AgentSecondContactNamePage, "test user")
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-second-contact-email")
    }

    "must return AgentHaveSecondContactPhone if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, true)
        .withPage(AgentSecondContactNamePage, "test user")
        .withPage(AgentSecondContactEmailPage, "t2@test.com")
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-second-contact-have-phone")
    }

    "must return AgentSecondContactNumber if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, true)
        .withPage(AgentSecondContactNamePage, "test user")
        .withPage(AgentSecondContactEmailPage, "t2@test.com")
        .withPage(AgentSecondContactHavePhonePage, true)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/change-second-contact-have-phone")
    }

    "must return ChangeAgentContactDetails if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, false)
        .withPage(IsMigratedAgentContactUpdatedPage, false)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/agent/agent-contact-details/check-answers")
    }

    "must return None if all mandatory values are available" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, true)
        .withPage(AgentSecondContactNamePage, "test user")
        .withPage(AgentSecondContactEmailPage, "t2@test.com")
        .withPage(AgentSecondContactHavePhonePage, true)
        .withPage(AgentSecondContactPhonePage, "8889988728")
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe false
    }

    "must return None if all mandatory values are available without second contact" in {
      val updatedUA = emptyUserAnswers
        .withPage(AgentFirstContactNamePage, "test")
        .withPage(AgentFirstContactEmailPage, "test@test.com")
        .withPage(AgentFirstContactHavePhonePage, false)
        .withPage(AgentHaveSecondContactPage, false)
      val changeUrl = AgentCheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe false
    }
  }
}

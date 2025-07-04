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

class CheckYourAnswersValidatorSpec extends SpecBase {
  "ChangeAnswersRedirectUrl" - {

    "must return ChangeName if all mandatory values are not available" in {
      val changeUrl = CheckYourAnswersValidator(emptyUserAnswers).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-name")
    }

    "must return ChangeEmail if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers.withPage(ContactNamePage, "test")
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-email")
    }

    "must return HavePhone if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers.withPage(ContactNamePage, "test").withPage(ContactEmailPage, "test@test.com")
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-have-phone")
    }

    "must return ContactPhone if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, true)
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-phone")
    }

    "must return HaveSecondContact if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-have-second-contact")
    }

    "must return SecondContactName if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
        .withPage(HaveSecondContactPage, true)
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-second-contact-name")
    }

    "must return SecondContactEmail if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, "test user")
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-second-contact-email")
    }

    "must return HaveSecondContactPhone if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, "test user")
        .withPage(SecondContactEmailPage, "t2@test.com")
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-second-contact-have-phone")
    }

    "must return SecondContactNumber if all mandatory values are not available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, "test user")
        .withPage(SecondContactEmailPage, "t2@test.com")
        .withPage(SecondContactHavePhonePage, true)
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe true
      changeUrl.get should equal("/send-a-country-by-country-report/change-contact/change-second-contact-phone")
    }

    "must return None if all mandatory values are available" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, true)
        .withPage(ContactPhonePage, "6677889922")
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, "test user")
        .withPage(SecondContactEmailPage, "t2@test.com")
        .withPage(SecondContactHavePhonePage, true)
        .withPage(SecondContactPhonePage, "8889988728")
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe false
    }

    "must return None if all mandatory values are available without second contact" in {
      val updatedUA = emptyUserAnswers
        .withPage(ContactNamePage, "test")
        .withPage(ContactEmailPage, "test@test.com")
        .withPage(HaveTelephonePage, false)
        .withPage(HaveSecondContactPage, false)
      val changeUrl = CheckYourAnswersValidator(updatedUA).changeAnswersRedirectUrl(CheckMode)

      changeUrl.isDefined shouldBe false
    }

  }
}

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

package viewmodels

import base.SpecBase
import pages.*
import play.api.i18n.Messages

class CheckYourAnswersHelperSpec extends SpecBase {

  implicit lazy val msgs: Messages = messages(app)

  "CheckYourAnswersHelper" - {

    "must handle a complete user journey with both primary and secondary contacts" in {
      val userAnswers = emptyUserAnswers
        .withPage(ContactNamePage, "John Smith")
        .withPage(ContactEmailPage, "john@example.com")
        .withPage(ContactPhonePage, "01234 567890")
        .withPage(HaveSecondContactPage, true)
        .withPage(SecondContactNamePage, "Jane Smith")
        .withPage(SecondContactEmailPage, "jane@example.com")
        .withPage(SecondContactPhonePage, "01234 567891")

      val helper = CheckYourAnswersHelper(userAnswers)

      val primaryDetails   = helper.getPrimaryContactDetails
      val secondaryDetails = helper.getSecondaryContactDetails

      primaryDetails must have length 3
      secondaryDetails must have length 4
    }

    "must handle a user journey with only primary contact" in {
      val userAnswers = emptyUserAnswers
        .withPage(ContactNamePage, "John Smith")
        .withPage(ContactEmailPage, "john@example.com")
        .withPage(ContactPhonePage, "01234 567890")
        .withPage(HaveSecondContactPage, false)

      val helper = CheckYourAnswersHelper(userAnswers)

      val primaryDetails   = helper.getPrimaryContactDetails
      val secondaryDetails = helper.getSecondaryContactDetails

      primaryDetails must have length 3
      secondaryDetails must have length 1
    }

    "getPrimaryContactDetails" - {
      "must return all primary contact details when all are provided" in {

        val userAnswers = emptyUserAnswers
          .withPage(ContactNamePage, "John Smith")
          .withPage(ContactEmailPage, "john@example.com")
          .withPage(ContactPhonePage, "01234 567890")

        val helper = CheckYourAnswersHelper(userAnswers)
        val result = helper.getPrimaryContactDetails

        result must have length 3
      }
    }
    "getSecondaryContactDetails" - {
      "must return all secondary contact details when all are provided" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSecondContactPage, true)
          .withPage(SecondContactNamePage, "Jane Smith")
          .withPage(SecondContactEmailPage, "jane@example.com")
          .withPage(SecondContactPhonePage, "01234 567891")

        val helper = CheckYourAnswersHelper(userAnswers)
        val result = helper.getSecondaryContactDetails

        result must have length 4
      }
    }

    "contactNamePage" - {
      "must return a SummaryListRow when contact name is provided" in {
        val userAnswers = emptyUserAnswers.withPage(ContactNamePage, "John Smith")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactNamePage()

        result must not be None
        result.value.actions must not be None
      }

      "must return None when contact name is not provided" in {
        val userAnswers = emptyUserAnswers
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactNamePage()

        result must be(None)
      }

      "must have change action with correct link" in {
        val userAnswers = emptyUserAnswers.withPage(ContactNamePage, "John Smith")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactNamePage()

        result.value.actions.value.items must have length 1
        result.value.actions.value.items.head.href must include("/change-name")
      }
    }

    "contactEmailPage" - {
      "must return a SummaryListRow when contact email is provided" in {
        val userAnswers = emptyUserAnswers.withPage(ContactEmailPage, "john@example.com")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactEmailPage()

        result must not be None
        result.value.actions must not be None
      }

      "must return None when contact email is not provided" in {
        val userAnswers = emptyUserAnswers
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactEmailPage()

        result must be(None)
      }

      "must have change action with correct link" in {
        val userAnswers = emptyUserAnswers.withPage(ContactEmailPage, "john@example.com")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactEmailPage()

        result.value.actions.value.items must have length 1
        result.value.actions.value.items.head.href must include("/change-email")
      }
    }

    "contactPhonePage" - {
      "must return a SummaryListRow with phone number when provided" in {
        val userAnswers = emptyUserAnswers.withPage(ContactPhonePage, "01234 567890")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactPhonePage()

        result must not be None
      }

      "must return a SummaryListRow with 'None' message when no phone provided" in {
        val userAnswers = emptyUserAnswers
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.contactPhonePage()

        result must not be None
        result.value.toString must include("None")

      }

    }

    "hasSecondContactPage" - {
      "must return a SummaryListRow when HaveSecondContactPage is set to true" in {
        val userAnswers = emptyUserAnswers.withPage(HaveSecondContactPage, true)
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.hasSecondContactPage()

        result must not be None
        result.value.toString must include("Yes")

      }

      "must return a value when HaveSecondContactPage is set to false" in {
        val userAnswers = emptyUserAnswers.withPage(HaveSecondContactPage, false)
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.hasSecondContactPage()

        result must not be None
        result.value.toString must include("No")

      }

    }

    "secondaryContactNamePage" - {
      "must return a SummaryListRow when secondary contact name is provided" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactNamePage, "Jane Smith")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.secondaryContactNamePage()

        result must not be None
      }

      "must return None when secondary contact name is not provided" in {
        val userAnswers = emptyUserAnswers
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.secondaryContactNamePage()

        result must be(None)
      }

    }

    "secondaryContactEmailPage" - {
      "must return a SummaryListRow when secondary contact email is provided" in {
        val userAnswers = emptyUserAnswers.withPage(SecondContactEmailPage, "jane@example.com")
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.secondaryContactEmailPage()

        result must not be None
      }

      "must return None when secondary contact email is not provided" in {
        val userAnswers = emptyUserAnswers
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.secondaryContactEmailPage()

        result must be(None)
      }

    }

    "secondaryContactPhonePage" - {
      "must return a SummaryListRow when HaveSecondContactPage is true and phone is provided" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSecondContactPage, true)
          .withPage(SecondContactPhonePage, "01234 567891")

        val helper = CheckYourAnswersHelper(userAnswers)
        val result = helper.secondaryContactPhonePage()

        result must not be None
      }

      "must return None when HaveSecondContactPage is false" in {
        val userAnswers = emptyUserAnswers.withPage(HaveSecondContactPage, false)
        val helper      = CheckYourAnswersHelper(userAnswers)

        val result = helper.secondaryContactPhonePage()

        result must be(None)
      }
    }

  }
}

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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: ContactDetailsNavigator = new ContactDetailsNavigator

  "Navigator" - {
    "in Normal mode" - {

      "must go from Contact Phone page to have second contact details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactPhonePage, NormalMode, answers)
              .mustBe(routes.HaveSecondContactController.onPageLoad(NormalMode))
        }
      }

      "must go from Contact Name page to Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactNamePage, NormalMode, answers)
              .mustBe(routes.ContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from Contact Email page to Have Telephone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactEmailPage, NormalMode, answers)
              .mustBe(routes.HaveTelephoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Have Phone page to Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveTelephonePage, true).success.value

            navigator
              .nextPage(HaveTelephonePage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Have Second Contact page to Second Contact Name page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveSecondContactPage, true).success.value

            navigator
              .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.SecondContactNameController.onPageLoad(NormalMode))
        }
      }

      "must go from Have Second Contact page to Change Organisation Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveSecondContactPage, false).success.value

            navigator
              .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Second Contact Name page to Second Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactNamePage, NormalMode, answers)
              .mustBe(routes.SecondContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from Second Contact Email page to Second Contact Have Phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactEmailPage, NormalMode, answers)
              .mustBe(routes.SecondContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Second Contact Have Phone page to Second Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(SecondContactHavePhonePage, true).success.value

            navigator
              .nextPage(SecondContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.SecondContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Second Contact Have Phone page to Change Organisation Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(SecondContactHavePhonePage, false).success.value

            navigator
              .nextPage(SecondContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Second Contact Phone page to Change Organisation Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactPhonePage, NormalMode, answers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }
    }

    "in Check mode" - {

      "must go from Contact Phone page to Change Organisation Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactPhonePage, CheckMode, answers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Have Telephone Page to Change Organisation Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveTelephonePage, false).success.value

            navigator
              .nextPage(ContactPhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Contact Name page to Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactNamePage, CheckMode, answers)
              .mustBe(routes.ContactEmailController.onPageLoad(CheckMode))
        }
      }

      "must go from Contact Email page to Have Telephone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactEmailPage, CheckMode, answers)
              .mustBe(routes.HaveTelephoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Have Phone page to Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveTelephonePage, true).success.value

            navigator
              .nextPage(HaveTelephonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Have Second Contact page to Second Contact Name page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveSecondContactPage, true).success.value

            navigator
              .nextPage(HaveSecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.SecondContactNameController.onPageLoad(CheckMode))
        }
      }

      "must go from Have Second Contact page to Change Organisation Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveSecondContactPage, false).success.value

            navigator
              .nextPage(HaveSecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Second Contact Name page to Second Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactNamePage, CheckMode, answers)
              .mustBe(routes.SecondContactEmailController.onPageLoad(CheckMode))
        }
      }

      "must go from Second Contact Email page to Second Contact Have Phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactEmailPage, CheckMode, answers)
              .mustBe(routes.SecondContactHavePhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Second Contact Have Phone page to Second Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(SecondContactHavePhonePage, true).success.value

            navigator
              .nextPage(SecondContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.SecondContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Second Contact Have Phone page to Change Organisation Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(SecondContactHavePhonePage, false).success.value

            navigator
              .nextPage(SecondContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }

      "must go from Second Contact Phone page to Change Organisation Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactPhonePage, CheckMode, answers)
              .mustBe(routes.ChangeContactDetailsController.onPageLoad())
        }
      }
    }
  }
}

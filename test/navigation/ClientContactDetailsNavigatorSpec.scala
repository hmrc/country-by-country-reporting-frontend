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
import controllers.client.routes
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Arbitrary.arbitrary
import pages._

class ClientContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator = new ClientContactDetailsNavigator

  "Navigator" - {
    "In normal mode" - {
      "Must go from client first contact name page to client first contact email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactNamePage, NormalMode, answers)
              .mustBe(routes.ClientFirstContactEmailController.onPageLoad(NormalMode))
        }
      }

      "Must go from client first contact email page to client first contact have phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactEmailPage, NormalMode, answers)
              .mustBe(routes.ClientFirstContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact have phone page- to -client first contact phone page- when YES is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveTelephonePage, value = true).success.value

            navigator
              .nextPage(HaveTelephonePage, NormalMode, updatedAnswers)
              .mustBe(routes.ClientFirstContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact have phone page- to -client have second contact details page- when NO is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(HaveTelephonePage, value = false).success.value

            navigator
              .nextPage(HaveTelephonePage, NormalMode, updatedAnswers)
              .mustBe(routes.ClientHaveSecondContactController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact phone page- to -client have second contact page-" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContactPhonePage, NormalMode, answers)
              .mustBe(routes.ClientHaveSecondContactController.onPageLoad(NormalMode))

        }
      }

      "Must go from -client have second contact page- to -check your answers- when NO is answered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(HaveSecondContactPage, value = false).success.value

            navigator
              .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client have second contact page- to -client second contact name- when YES is answered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(HaveSecondContactPage, value = true).success.value

            navigator
              .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.ClientSecondContactNameController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client second contact name page- to -client second contact email page-" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SecondContactNamePage, NormalMode, answers)
              .mustBe(routes.ClientSecondContactEmailController.onPageLoad(NormalMode))
        }
      }

      // use yesno method in navigator
      // yes go to second contact name
      // no go to check answers
    }
  }
}

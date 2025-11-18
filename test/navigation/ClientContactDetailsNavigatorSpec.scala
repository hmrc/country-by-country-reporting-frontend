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

package navigation

import base.SpecBase
import controllers.client.routes
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Arbitrary.arbitrary
import pages._

class ClientContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator = new ClientContactDetailsNavigator

  "Navigator" - {
    "In normal mode" - {

      "Must go from review client details page to have second contact page if answer is yes" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers
              .set(ReviewClientContactDetailsPage, value = true)
              .success
              .value
              .set(ContactNamePage, "name")
              .success
              .value
              .set(ContactEmailPage, "test@test.com")
              .success
              .value
          navigator
            .nextPage(ReviewClientContactDetailsPage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientHaveSecondContactController.onPageLoad(NormalMode))
        }
      }
      "Must go from review client details page to first contact name page if answer is no" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers
              .set(ReviewClientContactDetailsPage, value = false)
              .success
              .value
              .remove(ContactEmailPage)
              .get
              .remove(ContactNamePage)
              .get
          navigator
            .nextPage(ReviewClientContactDetailsPage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientFirstContactNameController.onPageLoad(NormalMode))
        }
      }
      "Must go from review client details page to there is a problem page if answer" +
        " is yes and userAnswers does not contain client name and email" in {
          forAll(arbitrary[UserAnswers]) { answers =>
            val updatedAnswers =
              answers
                .set(ReviewClientContactDetailsPage, value = true)
                .success
                .value
                .remove(ContactEmailPage)
                .get
                .remove(ContactNamePage)
                .get
            navigator
              .nextPage(ReviewClientContactDetailsPage, NormalMode, updatedAnswers)
              .mustBe(controllers.routes.ThereIsAProblemController.onPageLoad())
          }
        }
      "Must go from review client details page to have there is a problem page" +
        " if answer is no and userAnswers contains contact name and email" in {
          forAll(arbitrary[UserAnswers]) { answers =>
            val updatedAnswers =
              answers
                .set(ReviewClientContactDetailsPage, value = false)
                .success
                .value
                .set(ContactNamePage, "name")
                .success
                .value
                .set(ContactEmailPage, "test@test.com")
                .success
                .value
            navigator
              .nextPage(ReviewClientContactDetailsPage, NormalMode, updatedAnswers)
              .mustBe(controllers.routes.ThereIsAProblemController.onPageLoad())
          }
        }
      "Must go from client first contact name page to client first contact email page" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactNamePage, NormalMode, answers)
            .mustBe(routes.ClientFirstContactEmailController.onPageLoad(NormalMode))
        }
      }

      "Must go from client first contact email page to client first contact have phone page" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactEmailPage, NormalMode, answers)
            .mustBe(routes.ClientFirstContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact have phone page- to -client first contact phone page- when YES is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(HaveTelephonePage, value = true).success.value

          navigator
            .nextPage(HaveTelephonePage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientFirstContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact have phone page- to -client have second contact details page- when NO is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(HaveTelephonePage, value = false).success.value

          navigator
            .nextPage(HaveTelephonePage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientHaveSecondContactController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client first contact phone page- to -client have second contact page-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactPhonePage, NormalMode, answers)
            .mustBe(routes.ClientHaveSecondContactController.onPageLoad(NormalMode))

        }
      }

      "Must go from -client have second contact page- to -check your answers- when NO is answered" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers = answers.set(HaveSecondContactPage, value = false).success.value

          navigator
            .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client have second contact page- to -client second contact name- when YES is answered" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers = answers.set(HaveSecondContactPage, value = true).success.value

          navigator
            .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientSecondContactNameController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client second contact name page- to -client second contact email page-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactNamePage, NormalMode, answers)
            .mustBe(routes.ClientSecondContactEmailController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client second contact email page- to -client second contact have phone page-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactEmailPage, NormalMode, answers)
            .mustBe(routes.ClientSecondContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client second contact have phone page- to -client second contact phone page- when YES is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(SecondContactHavePhonePage, value = true).success.value

          navigator
            .nextPage(SecondContactHavePhonePage, NormalMode, updatedAnswers)
            .mustBe(routes.ClientSecondContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "Must go from -client second contact have phone page- to -check your answers- when NO is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(SecondContactHavePhonePage, value = false).success.value

          navigator
            .nextPage(SecondContactHavePhonePage, NormalMode, updatedAnswers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client second contact phone page- to -check your answers-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactPhonePage, NormalMode, answers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())

        }
      }
    }

    "In CHECK mode" - {
      "Must go from client first contact name page to client first contact email page" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactNamePage, CheckMode, answers)
            .mustBe(routes.ClientFirstContactEmailController.onPageLoad(CheckMode))
        }
      }

      "Must go from client first contact email page to client first contact have phone page" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactEmailPage, CheckMode, answers)
            .mustBe(routes.ClientFirstContactHavePhoneController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client first contact have phone page- to -client first contact phone page- when YES is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(HaveTelephonePage, value = true).success.value

          navigator
            .nextPage(HaveTelephonePage, CheckMode, updatedAnswers)
            .mustBe(routes.ClientFirstContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client first contact have phone page- to -check your answers- when NO is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(HaveTelephonePage, value = false).success.value

          navigator
            .nextPage(HaveTelephonePage, CheckMode, updatedAnswers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client first contact phone page- to -check your answers-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(ContactPhonePage, CheckMode, answers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())

        }
      }

      "Must go from -client have second contact page- to -check your answers- when NO is answered" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers = answers.set(HaveSecondContactPage, value = false).success.value

          navigator
            .nextPage(HaveSecondContactPage, CheckMode, updatedAnswers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client have second contact page- to -client second contact name- when YES is answered" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers = answers.set(HaveSecondContactPage, value = true).success.value

          navigator
            .nextPage(HaveSecondContactPage, CheckMode, updatedAnswers)
            .mustBe(routes.ClientSecondContactNameController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client second contact name page- to -client second contact email page-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactNamePage, CheckMode, answers)
            .mustBe(routes.ClientSecondContactEmailController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client second contact email page- to -client second contact have phone page-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactEmailPage, CheckMode, answers)
            .mustBe(routes.ClientSecondContactHavePhoneController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client second contact have phone page- to -client second contact phone page- when YES is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(SecondContactHavePhonePage, value = true).success.value

          navigator
            .nextPage(SecondContactHavePhonePage, CheckMode, updatedAnswers)
            .mustBe(routes.ClientSecondContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "Must go from -client second contact have phone page- to -check your answers- when NO is selected" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(SecondContactHavePhonePage, value = false).success.value

          navigator
            .nextPage(SecondContactHavePhonePage, CheckMode, updatedAnswers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())
        }
      }

      "Must go from -client second contact phone page- to -check your answers-" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          navigator
            .nextPage(SecondContactPhonePage, CheckMode, answers)
            .mustBe(routes.ChangeClientContactDetailsController.onPageLoad())

        }
      }
    }
  }
}

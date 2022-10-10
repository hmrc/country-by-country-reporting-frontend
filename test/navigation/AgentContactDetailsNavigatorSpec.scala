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
import controllers.agent.routes
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class AgentContactDetailsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator: AgentContactDetailsNavigator = new AgentContactDetailsNavigator

  "AgentContactDetailsNavigator" - {
    "in Normal mode" - {

      "must go from Agent First Contact Name page to Agent First Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactNamePage, NormalMode, answers)
              .mustBe(routes.AgentFirstContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent First Contact Email page to Agent First Contact Have Telephone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactEmailPage, NormalMode, answers)
              .mustBe(routes.AgentFirstContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent First Contact Have Phone page to Agent First Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentFirstContactHavePhonePage, true).success.value

            navigator
              .nextPage(AgentFirstContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.AgentFirstContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent First Contact Have Phone page to Agent have second contact details page when 'No' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentFirstContactHavePhonePage, false).success.value

            navigator
              .nextPage(AgentFirstContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.AgentHaveSecondContactController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent First Contact Phone page to Agent have second contact details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactPhonePage, NormalMode, answers)
              .mustBe(routes.AgentHaveSecondContactController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent Have Second Contact page to Agent Second Contact Name page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentHaveSecondContactPage, true).success.value

            navigator
              .nextPage(AgentHaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.AgentSecondContactNameController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent Have Second Contact page to Change Agent Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentHaveSecondContactPage, false).success.value

            navigator
              .nextPage(AgentHaveSecondContactPage, NormalMode, updatedAnswers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Agent Second Contact Name page to Agent Second Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactNamePage, NormalMode, answers)
              .mustBe(routes.AgentSecondContactEmailController.onPageLoad(NormalMode))
        }
      }

      "must go from Agent Second Contact Email page to Agent Second Contact Have Phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactEmailPage, NormalMode, answers)
              .mustBe(routes.AgentSecondContactHavePhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from AgentSecond Contact Have Phone page to AgentSecond Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentSecondContactHavePhonePage, true).success.value

            navigator
              .nextPage(AgentSecondContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.AgentSecondContactPhoneController.onPageLoad(NormalMode))
        }
      }

      "must go from AgentSecond Contact Have Phone page to Change Agent Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentSecondContactHavePhonePage, false).success.value

            navigator
              .nextPage(AgentSecondContactHavePhonePage, NormalMode, updatedAnswers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Second Contact Phone page to Change Organisation Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactPhonePage, NormalMode, answers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }
    }

    "in Check mode" - {

      "must go from Agent First Contact Name page to Agent First Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactNamePage, CheckMode, answers)
              .mustBe(routes.AgentFirstContactEmailController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent First Contact Email page to Agent First Contact Have Telephone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactEmailPage, CheckMode, answers)
              .mustBe(routes.AgentFirstContactHavePhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent First Contact Have Phone page to Agent First Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentFirstContactHavePhonePage, true).success.value

            navigator
              .nextPage(AgentFirstContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.AgentFirstContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent First Contact Have Telephone Page to Change Agent Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentFirstContactHavePhonePage, false).success.value

            navigator
              .nextPage(AgentFirstContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Agent First Contact Phone page to Change Agent Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentFirstContactPhonePage, CheckMode, answers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Agent Have Second Contact page to Agent Second Contact Name page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentHaveSecondContactPage, true).success.value

            navigator
              .nextPage(AgentHaveSecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.AgentSecondContactNameController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent Have Second Contact page to Change Agent Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentHaveSecondContactPage, false).success.value

            navigator
              .nextPage(AgentHaveSecondContactPage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Agent Second Contact Name page to Agent Second Contact Email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactNamePage, CheckMode, answers)
              .mustBe(routes.AgentSecondContactEmailController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent Second Contact Email page to Agent Second Contact Have Phone page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactEmailPage, CheckMode, answers)
              .mustBe(routes.AgentSecondContactHavePhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent Second Contact Have Phone page to Agent Second Contact Phone page when 'YES' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentSecondContactHavePhonePage, true).success.value

            navigator
              .nextPage(AgentSecondContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.AgentSecondContactPhoneController.onPageLoad(CheckMode))
        }
      }

      "must go from Agent Second Contact Have Phone page to Change Agent Details page when 'NO' is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers.set(AgentSecondContactHavePhonePage, false).success.value

            navigator
              .nextPage(AgentSecondContactHavePhonePage, CheckMode, updatedAnswers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }

      "must go from Agent Second Contact Phone page to Change Agent Details page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AgentSecondContactPhonePage, CheckMode, answers)
              .mustBe(routes.ChangeAgentContactDetailsController.onPageLoad())
        }
      }
    }
  }
}

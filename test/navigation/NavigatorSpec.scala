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
import controllers.agent.routes
import controllers.routes
import generators.Generators
import pages.*
import models.*
import models.ManageYourClients.{AddAClientToYourAgentServicesAccount, ChangeYourCBCAgentContactDetails, SelectAClient}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad
      }

      "must go from AgentIsYourClientPage to landing page when Yes" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(AgentIsThisYourClientPage, true).success.value

          navigator
            .nextPage(AgentIsThisYourClientPage, NormalMode, updatedAnswers)
            .mustBe(controllers.routes.IndexController.onPageLoad)
        }
      }

      "must go from AgentIsYourClientPage to ClientNotIdentified when No" in {
        forAll(arbitrary[UserAnswers]) { answers =>
          val updatedAnswers =
            answers.set(AgentIsThisYourClientPage, false).success.value

          navigator
            .nextPage(AgentIsThisYourClientPage, NormalMode, updatedAnswers)
            .mustBe(controllers.client.routes.ProblemCBCIdController.onPageLoad())
        }
      }

      "in Check mode" - {

        "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

          case object UnknownPage extends Page
          navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad
        }
      }

    }
  }

  "whatToDoNextNavigation" - {

    "must go to AgentClientIdController when SelectAClient is selected" in {
      forAll(arbitrary[UserAnswers]) { answers =>
        val updatedAnswers = answers.set(ManageYourClientsPage, SelectAClient).success.value

        navigator
          .whatToDoNextNavigation(updatedAnswers)
          .mustBe(controllers.agent.routes.AgentClientIdController.onPageLoad())
      }
    }

    "must go to ManageYourClientsController when AddAClientToYourAgentServicesAccount is selected" in {
      forAll(arbitrary[UserAnswers]) { answers =>
        val updatedAnswers = answers.set(ManageYourClientsPage, AddAClientToYourAgentServicesAccount).success.value

        navigator
          .whatToDoNextNavigation(updatedAnswers)
          .mustBe(controllers.agent.routes.ManageYourClientsController.onPageLoad())
      }
    }

    "must go to ChangeAgentContactDetailsController when ChangeYourCBCAgentContactDetails is selected" in {
      forAll(arbitrary[UserAnswers]) { answers =>
        val updatedAnswers = answers.set(ManageYourClientsPage, ChangeYourCBCAgentContactDetails).success.value

        navigator
          .whatToDoNextNavigation(updatedAnswers)
          .mustBe(controllers.agent.routes.ChangeAgentContactDetailsController.onPageLoad())
      }
    }

    "must go to ThereIsAProblemController when ManageYourClientsPage is not set" in {
      forAll(arbitrary[UserAnswers]) { answers =>
        val updatedAnswers = answers.remove(ManageYourClientsPage).success.value

        navigator
          .whatToDoNextNavigation(updatedAnswers)
          .mustBe(controllers.routes.ThereIsAProblemController.onPageLoad())
      }
    }
  }
}

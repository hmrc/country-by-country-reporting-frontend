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
    }
  }
}

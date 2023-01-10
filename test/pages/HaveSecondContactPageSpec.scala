/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class HaveSecondContactPageSpec extends PageBehaviours {

  "HaveSecondContactPage" - {

    beRetrievable[Boolean](HaveSecondContactPage)

    beSettable[Boolean](HaveSecondContactPage)

    beRemovable[Boolean](HaveSecondContactPage)

    "cleanup" - {

      "must remove SecondContactPages when there is a change of the answer from 'Yes' to 'No'" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(HaveSecondContactPage, true)
              .success
              .value
              .set(SecondContactNamePage, "name")
              .success
              .value
              .set(SecondContactEmailPage, "test@gmail.com")
              .success
              .value
              .set(SecondContactPhonePage, "112233445566")
              .success
              .value
              .set(HaveSecondContactPage, false)
              .success
              .value

            result.get(SecondContactNamePage) must not be defined
            result.get(SecondContactEmailPage) must not be defined
            result.get(SecondContactPhonePage) must not be defined
        }
      }

      "must retain SecondContactPages when there is a change of the answer to 'Yes'" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(SecondContactNamePage, "name")
              .success
              .value
              .set(SecondContactEmailPage, "test@gmail.com")
              .success
              .value
              .set(SecondContactPhonePage, "112233445566")
              .success
              .value
              .set(HaveSecondContactPage, true)
              .success
              .value

            result.get(SecondContactNamePage) mustBe defined
            result.get(SecondContactEmailPage) mustBe defined
            result.get(SecondContactPhonePage) mustBe defined
        }
      }
    }
  }
}

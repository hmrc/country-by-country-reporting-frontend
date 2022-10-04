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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryAgentContactTelephoneNumberPage: Arbitrary[AgentFirstContactPhonePage.type] =
    Arbitrary(AgentFirstContactPhonePage)

  implicit lazy val arbitraryAgentSecondContactEmailPage: Arbitrary[AgentSecondContactEmailPage.type] =
    Arbitrary(AgentSecondContactEmailPage)

  implicit lazy val arbitraryAgentSecondContactNamePage: Arbitrary[AgentSecondContactNamePage.type] =
    Arbitrary(AgentSecondContactNamePage)

  implicit lazy val arbitraryAgentFirstContactNamePage: Arbitrary[AgentFirstContactNamePage.type] =
    Arbitrary(AgentFirstContactNamePage)

  implicit lazy val arbitrarySecondContactEmailPage: Arbitrary[SecondContactEmailPage.type] =
    Arbitrary(SecondContactEmailPage)

  implicit lazy val arbitraryContactNamePage: Arbitrary[ContactNamePage.type] =
    Arbitrary(ContactNamePage)

  implicit lazy val arbitraryHaveTelephonePage: Arbitrary[HaveTelephonePage.type] =
    Arbitrary(HaveTelephonePage)

  implicit lazy val arbitraryContactPhonePage: Arbitrary[ContactPhonePage.type] =
    Arbitrary(ContactPhonePage)

  implicit lazy val arbitraryContactEmailPage: Arbitrary[ContactEmailPage.type] =
    Arbitrary(ContactEmailPage)

  implicit lazy val arbitraryHaveSecondContactPage: Arbitrary[HaveSecondContactPage.type] =
    Arbitrary(HaveSecondContactPage)

  implicit lazy val arbitrarySecondContactNamePage: Arbitrary[SecondContactNamePage.type] =
    Arbitrary(SecondContactNamePage)

  implicit lazy val arbitrarySecondContactHavePhonePage: Arbitrary[SecondContactHavePhonePage.type] =
    Arbitrary(SecondContactHavePhonePage)

  implicit lazy val arbitrarySecondContactPhonePage: Arbitrary[SecondContactPhonePage.type] =
    Arbitrary(SecondContactPhonePage)

}

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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryReviewClientContactDetailsPage: Arbitrary[ReviewClientContactDetailsPage.type] =
    Arbitrary(ReviewClientContactDetailsPage)

  implicit lazy val arbitraryWhatToDoNextPage: Arbitrary[WhatToDoNextPage.type] =
    Arbitrary(WhatToDoNextPage)

  implicit lazy val arbitraryAgentIsThisYourClientPage: Arbitrary[AgentIsThisYourClientPage.type] =
    Arbitrary(AgentIsThisYourClientPage)

  implicit lazy val arbitraryAgentFirstContactNamePage: Arbitrary[AgentFirstContactNamePage.type] =
    Arbitrary(AgentFirstContactNamePage)

  implicit lazy val arbitraryAgentFirstContactEmailPage: Arbitrary[AgentFirstContactEmailPage.type] =
    Arbitrary(AgentFirstContactEmailPage)

  implicit lazy val arbitraryAgentFirstContactHavePhonePage: Arbitrary[AgentFirstContactHavePhonePage.type] =
    Arbitrary(AgentFirstContactHavePhonePage)

  implicit lazy val arbitraryAgentFirstContactPhonePage: Arbitrary[AgentFirstContactPhonePage.type] =
    Arbitrary(AgentFirstContactPhonePage)

  implicit lazy val arbitraryAgentHaveSecondContactPage: Arbitrary[AgentHaveSecondContactPage.type] =
    Arbitrary(AgentHaveSecondContactPage)

  implicit lazy val arbitraryAgentSecondContactNamePage: Arbitrary[AgentSecondContactNamePage.type] =
    Arbitrary(AgentSecondContactNamePage)

  implicit lazy val arbitraryAgentSecondContactEmailPage: Arbitrary[AgentSecondContactEmailPage.type] =
    Arbitrary(AgentSecondContactEmailPage)

  implicit lazy val arbitraryAgentSecondContactHavePhonePage: Arbitrary[AgentSecondContactHavePhonePage.type] =
    Arbitrary(AgentSecondContactHavePhonePage)

  implicit lazy val arbitraryAgentSecondContactPhonePage: Arbitrary[AgentSecondContactPhonePage.type] =
    Arbitrary(AgentSecondContactPhonePage)

  implicit lazy val arbitraryContactNamePage: Arbitrary[ContactNamePage.type] =
    Arbitrary(ContactNamePage)

  implicit lazy val arbitraryContactEmailPage: Arbitrary[ContactEmailPage.type] =
    Arbitrary(ContactEmailPage)

  implicit lazy val arbitraryHaveTelephonePage: Arbitrary[HaveTelephonePage.type] =
    Arbitrary(HaveTelephonePage)

  implicit lazy val arbitraryContactPhonePage: Arbitrary[ContactPhonePage.type] =
    Arbitrary(ContactPhonePage)

  implicit lazy val arbitraryHaveSecondContactPage: Arbitrary[HaveSecondContactPage.type] =
    Arbitrary(HaveSecondContactPage)

  implicit lazy val arbitrarySecondContactNamePage: Arbitrary[SecondContactNamePage.type] =
    Arbitrary(SecondContactNamePage)

  implicit lazy val arbitrarySecondContactEmailPage: Arbitrary[SecondContactEmailPage.type] =
    Arbitrary(SecondContactEmailPage)

  implicit lazy val arbitrarySecondContactHavePhonePage: Arbitrary[SecondContactHavePhonePage.type] =
    Arbitrary(SecondContactHavePhonePage)

  implicit lazy val arbitrarySecondContactPhonePage: Arbitrary[SecondContactPhonePage.type] =
    Arbitrary(SecondContactPhonePage)

}

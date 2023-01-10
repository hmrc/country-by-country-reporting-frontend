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
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryAgentIsThisYourClientUserAnswersEntry: Arbitrary[(AgentIsThisYourClientPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentIsThisYourClientPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentFirstContactNameUserAnswersEntry: Arbitrary[(AgentFirstContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentFirstContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentFirstContactEmailUserAnswersEntry: Arbitrary[(AgentFirstContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentFirstContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentFirstContactHavePhoneAnswersEntry: Arbitrary[(AgentFirstContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentFirstContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentFirstContactPhoneUserAnswersEntry: Arbitrary[(AgentFirstContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentFirstContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentHaveSecondContactUserAnswersEntry: Arbitrary[(AgentHaveSecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentHaveSecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentSecondContactNameUserAnswersEntry: Arbitrary[(AgentSecondContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentSecondContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentSecondContactEmailUserAnswersEntry: Arbitrary[(AgentSecondContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentSecondContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentSecondContactHavePhoneUserAnswersEntry: Arbitrary[(AgentSecondContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentSecondContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentSecondContactPhoneUserAnswersEntry: Arbitrary[(AgentSecondContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentSecondContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(ContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactEmailUserAnswersEntry: Arbitrary[(ContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHaveTelephoneUserAnswersEntry: Arbitrary[(HaveTelephonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HaveTelephonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactPhoneUserAnswersEntry: Arbitrary[(ContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHaveSecondContactUserAnswersEntry: Arbitrary[(HaveSecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HaveSecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactNameUserAnswersEntry: Arbitrary[(SecondContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactEmailUserAnswersEntry: Arbitrary[(SecondContactEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondContactEmailPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactHavePhoneUserAnswersEntry: Arbitrary[(SecondContactHavePhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondContactHavePhonePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondContactPhoneUserAnswersEntry: Arbitrary[(SecondContactPhonePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondContactPhonePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

}

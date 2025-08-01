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

package generators

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(ReviewContactDetailsPage.type, JsValue)] ::
      arbitrary[(ReviewClientContactDetailsPage.type, JsValue)] ::
      arbitrary[(ManageYourClientsPage.type, JsValue)] ::
      arbitrary[(AgentIsThisYourClientPage.type, JsValue)] ::
      arbitrary[(AgentFirstContactNamePage.type, JsValue)] ::
      arbitrary[(AgentFirstContactEmailPage.type, JsValue)] ::
      arbitrary[(AgentFirstContactHavePhonePage.type, JsValue)] ::
      arbitrary[(AgentFirstContactPhonePage.type, JsValue)] ::
      arbitrary[(AgentHaveSecondContactPage.type, JsValue)] ::
      arbitrary[(AgentSecondContactNamePage.type, JsValue)] ::
      arbitrary[(AgentSecondContactEmailPage.type, JsValue)] ::
      arbitrary[(AgentSecondContactHavePhonePage.type, JsValue)] ::
      arbitrary[(AgentSecondContactPhonePage.type, JsValue)] ::
      arbitrary[(ContactNamePage.type, JsValue)] ::
      arbitrary[(ContactEmailPage.type, JsValue)] ::
      arbitrary[(HaveTelephonePage.type, JsValue)] ::
      arbitrary[(ContactPhonePage.type, JsValue)] ::
      arbitrary[(HaveSecondContactPage.type, JsValue)] ::
      arbitrary[(SecondContactNamePage.type, JsValue)] ::
      arbitrary[(SecondContactEmailPage.type, JsValue)] ::
      arbitrary[(SecondContactHavePhonePage.type, JsValue)] ::
      arbitrary[(SecondContactPhonePage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}

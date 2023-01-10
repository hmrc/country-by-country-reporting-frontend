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

package models.subscription

import base.SpecBase
import play.api.libs.json._

class ContactInformationModelSpec extends SpecBase {

  "ContactInformation" - {
    "must serialise and de-serialise ContactInformation" in {

      val contactInformationJson: String =
        """{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"organisation": {
          |"organisationName": "orgName"
          |}
          |}""".stripMargin

      val expectedJson =
        """{"organisation":{"organisationName":"orgName"},"email":"","phone":"","mobile":""}""".stripMargin
      val json: JsValue =
        Json.parse(contactInformationJson)
      val contactInformation = json.as[ContactInformation]
      Json.toJson(contactInformation) mustBe Json.parse(expectedJson)
    }

    "must fail to serialise and de-serialise ContactInformation when individual contact details supplied" in {

      val contactInformationJson: String =
        """{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"individual": {
          |"lastName": "Last",
          |"firstName": "First"
          |}
          |}""".stripMargin

      Json.parse(contactInformationJson).validate[ContactInformation] mustBe
        JsError(List((JsPath \ "organisation" \ "organisationName", List(JsonValidationError(List("error.path.missing"))))))

    }
  }
}

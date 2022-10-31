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

package models.agentSubscription

import base.SpecBase
import play.api.libs.json._

class AgentContactInformationModelSpec extends SpecBase {

  "AgentContactInformation" - {
    "must serialise and de-serialise ContactInformation" in {

      val agentContactInformationJson: String =
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
        Json.parse(agentContactInformationJson)
      val agentContactInformation = json.as[AgentContactInformation]
      Json.toJson(agentContactInformation) mustBe Json.parse(expectedJson)
    }

    "must fail to serialise and de-serialise ContactInformation when individual contact details supplied" in {

      val agentContactInformationJson: String =
        """{
          |"email": "",
          |"phone": "",
          |"mobile": "",
          |"individual": {
          |"lastName": "Last",
          |"firstName": "First"
          |}
          |}""".stripMargin

      Json.parse(agentContactInformationJson).validate[AgentContactInformation] mustBe
        JsError(List((JsPath \ "organisation" \ "organisationName", List(JsonValidationError(List("error.path.missing"))))))

    }
  }
}

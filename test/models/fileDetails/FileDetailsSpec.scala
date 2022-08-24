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

package models.fileDetails

import base.SpecBase
import generators.Generators
import models.{fileDetails, ConversationId}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

import java.time.LocalDateTime

class FileDetailsSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "FileDetails" - {
    "Serialise to Json" in {

      val date            = LocalDateTime.now
      val validationError = Arbitrary.arbitrary[ValidationErrors].sample.value

      val fileDetail1    = FileDetails("test1.xml", "messageRefId1", date, date, Pending, ConversationId("XGD11111"))
      val fileDetail2    = fileDetails.FileDetails("test2.xml", "messageRefId2", date, date.plusSeconds(11), Rejected(validationError), ConversationId("XGD11111"))
      val fileDetail3    = fileDetails.FileDetails("test3.xml", "messageRefId3", date, date.plusSeconds(25), Accepted, ConversationId("XGD11111"))
      val expectedResult = Seq(fileDetail1, fileDetail2, fileDetail3)

      val json = Json.toJson(Seq(fileDetail1, fileDetail2, fileDetail3))

      json.as[Seq[FileDetails]] mustBe expectedResult
    }
  }
}

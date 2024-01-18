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

package models.fileDetails

import base.SpecBase
import generators.Generators
import models.ConversationId
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

import java.time.LocalDateTime

class FileDetailsSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val submittedDate = "2024-01-01T00:00:00"
  private val modifiedDate  = "2024-01-01T00:00:10"

  "FileDetails" - {

    "must serialise to json with a pending status" in {
      val json = Json.toJson(
        FileDetails(
          "test1.xml",
          "messageRefId1",
          "Reporting Entity",
          LocalDateTime.parse(submittedDate),
          LocalDateTime.parse(modifiedDate),
          Pending,
          ConversationId("XGD11111")
        )
      )

      json mustEqual Json.obj(
        "name"                -> "test1.xml",
        "messageRefId"        -> "messageRefId1",
        "reportingEntityName" -> "Reporting Entity",
        "submitted"           -> submittedDate,
        "lastUpdated"         -> modifiedDate,
        "status" -> Json.obj(
          "Pending" -> Json.obj()
        ),
        "conversationId" -> "XGD11111"
      )
    }

    "must serialise to json with a rejected status" in {
      val validationErrors = Arbitrary.arbitrary[FileValidationErrors].sample.value

      val json = Json.toJson(
        FileDetails(
          "test2.xml",
          "messageRefId2",
          "Reporting Entity",
          LocalDateTime.parse(submittedDate),
          LocalDateTime.parse(modifiedDate),
          Rejected(validationErrors),
          ConversationId("XGD11111")
        )
      )

      json mustEqual Json.obj(
        "name"                -> "test2.xml",
        "messageRefId"        -> "messageRefId2",
        "reportingEntityName" -> "Reporting Entity",
        "submitted"           -> submittedDate,
        "lastUpdated"         -> modifiedDate,
        "status" -> Json.obj(
          "Rejected" -> Json.obj(
            "error" -> Json.toJson(validationErrors)
          )
        ),
        "conversationId" -> "XGD11111"
      )
    }

    "must serialise to json with a accepted status" in {
      val json = Json.toJson(
        FileDetails(
          "test3.xml",
          "messageRefId3",
          "Reporting Entity",
          LocalDateTime.parse(submittedDate),
          LocalDateTime.parse(modifiedDate),
          Accepted,
          ConversationId("XGD11111")
        )
      )

      json mustEqual Json.obj(
        "name"                -> "test3.xml",
        "messageRefId"        -> "messageRefId3",
        "reportingEntityName" -> "Reporting Entity",
        "submitted"           -> submittedDate,
        "lastUpdated"         -> modifiedDate,
        "status" -> Json.obj(
          "Accepted" -> Json.obj()
        ),
        "conversationId" -> "XGD11111"
      )
    }
  }
}

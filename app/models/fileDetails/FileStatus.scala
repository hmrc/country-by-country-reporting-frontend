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

import play.api.libs.json.*

sealed trait FileStatus

case object Pending extends FileStatus
case object Accepted extends FileStatus

case object RejectedSDES extends FileStatus
case object RejectedSDESVirus extends FileStatus

case class Rejected(error: FileValidationErrors) extends FileStatus {
  override def toString: String = "Rejected"
}

object FileStatus {
  given Format[FileStatus] = Format(
    Reads {
      case obj: JsObject if obj.keys == Set("Pending") =>
        JsSuccess(Pending)

      case obj: JsObject if obj.keys == Set("Accepted") =>
        JsSuccess(Accepted)

      case obj: JsObject if obj.keys == Set("RejectedSDES") =>
        JsSuccess(RejectedSDES)

      case obj: JsObject if obj.keys == Set("RejectedSDESVirus") =>
        JsSuccess(RejectedSDESVirus)

      case obj: JsObject if obj.keys == Set("Rejected") =>
        (obj("Rejected") \ "error")
          .validate[FileValidationErrors]
          .map(Rejected.apply)

      case other =>
        JsError(s"Invalid FileStatus JSON: $other")
    },
    Writes {
      case Pending =>
        Json.obj("Pending" -> Json.obj())

      case Accepted =>
        Json.obj("Accepted" -> Json.obj())

      case RejectedSDES =>
        Json.obj("RejectedSDES" -> Json.obj())

      case RejectedSDESVirus =>
        Json.obj("RejectedSDESVirus" -> Json.obj())

      case Rejected(err) =>
        Json.obj("Rejected" -> Json.obj("error" -> Json.toJson(err)))
    }
  )
}

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

package models

import play.api.libs.json._

sealed trait MessageTypeIndic
case object CBC401 extends MessageTypeIndic
case object CBC402 extends MessageTypeIndic

object MessageTypeIndic {

  implicit val writes: Writes[MessageTypeIndic] = Writes[MessageTypeIndic] {
    case CBC401 => JsString("CBC401")
    case CBC402 => JsString("CBC402")
  }

  implicit val reads: Reads[MessageTypeIndic] = Reads[MessageTypeIndic] {
    case JsString("CBC401") => JsSuccess(CBC401)
    case JsString("CBC402") => JsSuccess(CBC402)
    case value              => JsError(s"Unexpected value of _type: $value")
  }
}

case class MessageSpecData(messageRefId: String, messageTypeIndic: MessageTypeIndic, reportingEntityName: String)

object MessageSpecData {
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]
}

case class ValidatedFileData(fileName: String, messageSpecData: MessageSpecData)

object ValidatedFileData {
  implicit val format: OFormat[ValidatedFileData] = Json.format[ValidatedFileData]
}

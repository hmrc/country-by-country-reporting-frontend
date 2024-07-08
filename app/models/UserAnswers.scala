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

package models

import models.crypto.SensitiveJsObject
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable}
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: String,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  def mongoFormat(encryptionEnabled: Boolean)(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] = {
    implicit val sensitiveFormat: Format[SensitiveJsObject] = {
      if (encryptionEnabled) {
        JsonEncryption.sensitiveEncrypterDecrypter(SensitiveJsObject.apply)
      } else {
        Json.format[SensitiveJsObject]
      }
    }
    val reads: Reads[UserAnswers] =
      (
        (__ \ "_id").read[String] and
          (__ \ "data").read[SensitiveJsObject] and
          (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(
        (id, data, lastUpdated) => UserAnswers(id, data.decryptedValue, lastUpdated)
      )
    val writes: OWrites[UserAnswers] =
      (
        (__ \ "_id").write[String] and
          (__ \ "data").write[SensitiveJsObject] and
          (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(
        ua => (ua.id, SensitiveJsObject(ua.data), ua.lastUpdated)
      )
    OFormat(reads, writes)
  }
}

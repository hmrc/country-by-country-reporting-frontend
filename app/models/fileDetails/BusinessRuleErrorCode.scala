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

import play.api.libs.json.{__, JsString, Reads, Writes}

sealed abstract class BusinessRuleErrorCode(val code: String)

object BusinessRuleErrorCode {

  case object FailedSchemaValidation extends BusinessRuleErrorCode("50007")
  case object InvalidMessageRefIDFormat extends BusinessRuleErrorCode("50008")
  case object MessageRefIDHasAlreadyBeenUsed extends BusinessRuleErrorCode("50009")
  case object FileContainsTestDataForProductionEnvironment extends BusinessRuleErrorCode("50010")
  case object NotMeantToBeReceivedByTheIndicatedJurisdiction extends BusinessRuleErrorCode("50012")
  case object DocRefIDAlreadyUsed extends BusinessRuleErrorCode("80000")
  case object DocRefIDFormat extends BusinessRuleErrorCode("80001")
  case object CorrDocRefIdUnknown extends BusinessRuleErrorCode("80002")
  case object CorrDocRefIdNoLongerValid extends BusinessRuleErrorCode("80003")
  case object CorrDocRefIdForNewData extends BusinessRuleErrorCode("80004")
  case object MissingCorrDocRefId extends BusinessRuleErrorCode("80005")
  case object ResendOption extends BusinessRuleErrorCode("80008")
  case object DeleteParentRecord extends BusinessRuleErrorCode("80009")
  case object MessageTypeIndic extends BusinessRuleErrorCode("80010")
  case object CorrDocRefIDTwiceInSameMessage extends BusinessRuleErrorCode("80011")
  case object UnknownDocRefID extends BusinessRuleErrorCode("80013")
  case object DocRefIDIsNoLongerValid extends BusinessRuleErrorCode("80014")

  case object ChangeReportingPeriod extends BusinessRuleErrorCode("CBCErrorCode12")

  case object CurrencyCodeInitial extends BusinessRuleErrorCode("CBCErrorCode24a")

  case object CurrencyCodeCorrection extends BusinessRuleErrorCode("CBCErrorCode24b")

  case object CustomError extends BusinessRuleErrorCode("99999")
  case class UnknownErrorCode(override val code: String) extends BusinessRuleErrorCode(code)

  val fileErrorCodesForProblemStatus: Seq[BusinessRuleErrorCode] = Seq(
    FailedSchemaValidation,
    NotMeantToBeReceivedByTheIndicatedJurisdiction
  )

  val values: Seq[BusinessRuleErrorCode] = Seq(
    FailedSchemaValidation,
    InvalidMessageRefIDFormat,
    MessageRefIDHasAlreadyBeenUsed,
    NotMeantToBeReceivedByTheIndicatedJurisdiction,
    FileContainsTestDataForProductionEnvironment,
    DocRefIDAlreadyUsed,
    DocRefIDFormat,
    CorrDocRefIdUnknown,
    CorrDocRefIdNoLongerValid,
    CorrDocRefIdForNewData,
    MissingCorrDocRefId,
    ResendOption,
    DeleteParentRecord,
    MessageTypeIndic,
    CorrDocRefIDTwiceInSameMessage,
    UnknownDocRefID,
    DocRefIDIsNoLongerValid,
    ChangeReportingPeriod,
    CurrencyCodeInitial,
    CurrencyCodeCorrection,
    CustomError
  )

  implicit val writes: Writes[BusinessRuleErrorCode] = Writes[BusinessRuleErrorCode] {
    x =>
      JsString(x.code)
  }

  implicit val reads: Reads[BusinessRuleErrorCode] = __.read[String].map {
    case "50007"   => FailedSchemaValidation
    case "50008"   => InvalidMessageRefIDFormat
    case "50009"   => MessageRefIDHasAlreadyBeenUsed
    case "50010"   => FileContainsTestDataForProductionEnvironment
    case "50012"   => NotMeantToBeReceivedByTheIndicatedJurisdiction
    case "80000"   => DocRefIDAlreadyUsed
    case "80001"   => DocRefIDFormat
    case "80002"   => CorrDocRefIdUnknown
    case "80003"   => CorrDocRefIdNoLongerValid
    case "80004"   => CorrDocRefIdForNewData
    case "80005"   => MissingCorrDocRefId
    case "80008"   => ResendOption
    case "80009"   => DeleteParentRecord
    case "80010"   => MessageTypeIndic
    case "80011"   => CorrDocRefIDTwiceInSameMessage
    case "80013"   => UnknownDocRefID
    case "80014"   => DocRefIDIsNoLongerValid
    case "12"      => ChangeReportingPeriod
    case "24"      => CurrencyCodeInitial
    case "24b"     => CurrencyCodeCorrection
    case "99999"   => CustomError
    case otherCode => UnknownErrorCode(otherCode)
  }
}

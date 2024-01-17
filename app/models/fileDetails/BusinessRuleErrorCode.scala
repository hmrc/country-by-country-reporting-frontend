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

  case object MoreThanOneCBCBody extends BusinessRuleErrorCode("1")
  case object MessageSpecLargerThanSendEntity extends BusinessRuleErrorCode("2")
  case object DocRefIDMustMatchMessageRefID extends BusinessRuleErrorCode("3")
  case object MessageRefIDFileNameMatch extends BusinessRuleErrorCode("4")
  case object ReportingPeriodMatchYear extends BusinessRuleErrorCode("5")
  case object CBCIDIncorrect extends BusinessRuleErrorCode("6")
  case object MessageTypeIndicMatch extends BusinessRuleErrorCode("7")
  case object XMLTimestampMatch extends BusinessRuleErrorCode("8")
  case object TINMatch extends BusinessRuleErrorCode("9")
  case object DocTypeIndicMatch extends BusinessRuleErrorCode("10")
  case object XMLParentGroupMatch extends BusinessRuleErrorCode("11")
  case object ReportingPeriodCantChange extends BusinessRuleErrorCode("12")
  case object ReportingPeriodMatchEndDate extends BusinessRuleErrorCode("13")
  case object MultipleInitialReportingPeriods extends BusinessRuleErrorCode("14")
  case object PreviousSubmissionReportingTypeOverlap extends BusinessRuleErrorCode("15a")
  case object PreviousSubmissionReportingTypeOverlap2 extends BusinessRuleErrorCode("15b")
  case object ReportingPeriodStartBeforeEnd extends BusinessRuleErrorCode("16")
  case object OldestAllowableReportingPeriod extends BusinessRuleErrorCode("17")
  case object FutureReportingPeriod extends BusinessRuleErrorCode("18")
  case object ReportingEntityTINFormat extends BusinessRuleErrorCode("20")
  case object ConstEntityMatchInitial extends BusinessRuleErrorCode("21a")
  case object ConstEntityMatchCorrection extends BusinessRuleErrorCode("21b")
  case object DuplicationOfConstEntity extends BusinessRuleErrorCode("22a")
  case object DuplicationOfConstEntity2 extends BusinessRuleErrorCode("22b")
  case object CurrencyCodeInitial extends BusinessRuleErrorCode("23a")
  case object CurrencyCodeCorrection extends BusinessRuleErrorCode("23b")
  case object ConstEntitiesRoleInitial extends BusinessRuleErrorCode("24a")
  case object ConstEntitiesRoleCorrection extends BusinessRuleErrorCode("24b")
  case object ReportingRoleIsCBC701Initial extends BusinessRuleErrorCode("25a")
  case object ReportingRoleIsCBC701Correction extends BusinessRuleErrorCode("25b")
  case object ReportingRoleIsCBC702Initial extends BusinessRuleErrorCode("26a")
  case object ReportingRoleIsCBC702Correction extends BusinessRuleErrorCode("26b")
  case object ReportingRoleIsCBC702Initial2 extends BusinessRuleErrorCode("27a")
  case object ReportingRoleIsCBC702Correction2 extends BusinessRuleErrorCode("27b")
  case object WhiteSpaceSchemaValidation extends BusinessRuleErrorCode("28")
  case object BizActivitiesCBC513 extends BusinessRuleErrorCode("29")
  case object NegativeNumberOfEmployees extends BusinessRuleErrorCode("30")
  case object Exceeding10mEmployees extends BusinessRuleErrorCode("31")
  case object InitialMustContainOneCBCReport extends BusinessRuleErrorCode("32")
  case object OECD0CorrDocRefMustNotBePresent extends BusinessRuleErrorCode("33")
  case object MessageTypeIndicCBC402andOECD0 extends BusinessRuleErrorCode("34")
  case object MessageTypeIndicCBC401DocTypeOnlyOECD1 extends BusinessRuleErrorCode("35")
  case object SchemaVersionNumber extends BusinessRuleErrorCode("36")
  case object TINProvidedIssuedByAttributeMustBeCompleted extends BusinessRuleErrorCode("37")
  case object SendingEntityINIsAMandatoryField extends BusinessRuleErrorCode("38")
  case object SpecificReportingRoleCantMatchSpecificConstEntityRole extends BusinessRuleErrorCode("39a")
  case object SpecificReportingRoleCantMatchSpecificConstEntityRole2 extends BusinessRuleErrorCode("39b")
  case object SpecificReportingRoleCantMatchSpecificConstEntityRole3 extends BusinessRuleErrorCode("40a")
  case object SpecificReportingRoleCantMatchSpecificConstEntityRole4 extends BusinessRuleErrorCode("40b")
  case object ReportingRoleOECD0IsNotTheSame extends BusinessRuleErrorCode("42")
  case object ReportingRoleOECD0IsNotTheSame2 extends BusinessRuleErrorCode("43")

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
    MoreThanOneCBCBody,
    MessageSpecLargerThanSendEntity,
    DocRefIDMustMatchMessageRefID,
    MessageRefIDFileNameMatch,
    ReportingPeriodMatchYear,
    CBCIDIncorrect,
    MessageTypeIndicMatch,
    XMLTimestampMatch,
    TINMatch,
    DocTypeIndicMatch,
    XMLParentGroupMatch,
    ReportingPeriodCantChange,
    ReportingPeriodMatchEndDate,
    MultipleInitialReportingPeriods,
    PreviousSubmissionReportingTypeOverlap,
    PreviousSubmissionReportingTypeOverlap2,
    ReportingPeriodStartBeforeEnd,
    OldestAllowableReportingPeriod,
    FutureReportingPeriod,
    ReportingEntityTINFormat,
    ConstEntityMatchInitial,
    ConstEntityMatchCorrection,
    DuplicationOfConstEntity,
    DuplicationOfConstEntity2,
    CurrencyCodeInitial,
    CurrencyCodeCorrection,
    ConstEntitiesRoleInitial,
    ConstEntitiesRoleCorrection,
    ReportingRoleIsCBC701Initial,
    ReportingRoleIsCBC701Correction,
    ReportingRoleIsCBC702Initial,
    ReportingRoleIsCBC702Correction,
    ReportingRoleIsCBC702Initial2,
    ReportingRoleIsCBC702Correction2,
    WhiteSpaceSchemaValidation,
    BizActivitiesCBC513,
    NegativeNumberOfEmployees,
    Exceeding10mEmployees,
    InitialMustContainOneCBCReport,
    OECD0CorrDocRefMustNotBePresent,
    MessageTypeIndicCBC402andOECD0,
    MessageTypeIndicCBC401DocTypeOnlyOECD1,
    SchemaVersionNumber,
    TINProvidedIssuedByAttributeMustBeCompleted,
    SendingEntityINIsAMandatoryField,
    SpecificReportingRoleCantMatchSpecificConstEntityRole,
    SpecificReportingRoleCantMatchSpecificConstEntityRole2,
    SpecificReportingRoleCantMatchSpecificConstEntityRole3,
    SpecificReportingRoleCantMatchSpecificConstEntityRole4,
    ReportingRoleOECD0IsNotTheSame,
    ReportingRoleOECD0IsNotTheSame2,
    CustomError
  )

  implicit val writes: Writes[BusinessRuleErrorCode] = Writes[BusinessRuleErrorCode] {
    x =>
      JsString(x.code)
  }

  implicit val reads: Reads[BusinessRuleErrorCode] = __.read[String].map {
    case "50007"           => FailedSchemaValidation
    case "50008"           => InvalidMessageRefIDFormat
    case "50009"           => MessageRefIDHasAlreadyBeenUsed
    case "50010"           => FileContainsTestDataForProductionEnvironment
    case "50012"           => NotMeantToBeReceivedByTheIndicatedJurisdiction
    case "80000"           => DocRefIDAlreadyUsed
    case "80001"           => DocRefIDFormat
    case "80002"           => CorrDocRefIdUnknown
    case "80003"           => CorrDocRefIdNoLongerValid
    case "80004"           => CorrDocRefIdForNewData
    case "80005"           => MissingCorrDocRefId
    case "80008"           => ResendOption
    case "80009"           => DeleteParentRecord
    case "80010"           => MessageTypeIndic
    case "80011"           => CorrDocRefIDTwiceInSameMessage
    case "80013"           => UnknownDocRefID
    case "80014"           => DocRefIDIsNoLongerValid
    case "CBCErrorCode1"   => MoreThanOneCBCBody
    case "CBCErrorCode2"   => MessageSpecLargerThanSendEntity
    case "CBCErrorCode3"   => DocRefIDMustMatchMessageRefID
    case "CBCErrorCode4"   => MessageRefIDFileNameMatch
    case "CBCErrorCode5"   => ReportingPeriodMatchYear
    case "CBCErrorCode6"   => CBCIDIncorrect
    case "CBCErrorCode7"   => MessageTypeIndicMatch
    case "CBCErrorCode8"   => XMLTimestampMatch
    case "CBCErrorCode9"   => TINMatch
    case "CBCErrorCode10"  => DocTypeIndicMatch
    case "CBCErrorCode11"  => XMLParentGroupMatch
    case "CBCErrorCode12"  => ReportingPeriodCantChange
    case "CBCErrorCode13"  => ReportingPeriodMatchEndDate
    case "CBCErrorCode14"  => MultipleInitialReportingPeriods
    case "CBCErrorCode15a" => PreviousSubmissionReportingTypeOverlap
    case "CBCErrorCode15b" => PreviousSubmissionReportingTypeOverlap2
    case "CBCErrorCode16"  => ReportingPeriodStartBeforeEnd
    case "CBCErrorCode17"  => OldestAllowableReportingPeriod
    case "CBCErrorCode18"  => FutureReportingPeriod
    case "CBCErrorCode20"  => ReportingEntityTINFormat
    case "CBCErrorCode21a" => ConstEntityMatchInitial
    case "CBCErrorCode21b" => ConstEntityMatchCorrection
    case "CBCErrorCode22a" => DuplicationOfConstEntity
    case "CBCErrorCode22b" => DuplicationOfConstEntity2
    case "CBCErrorCode23a" => CurrencyCodeInitial
    case "CBCErrorCode23b" => CurrencyCodeCorrection
    case "CBCErrorCode24a" => ConstEntitiesRoleInitial
    case "CBCErrorCode24b" => ConstEntitiesRoleCorrection
    case "CBCErrorCode25a" => ReportingRoleIsCBC701Initial
    case "CBCErrorCode25b" => ReportingRoleIsCBC701Correction
    case "CBCErrorCode26a" => ReportingRoleIsCBC702Initial
    case "CBCErrorCode26b" => ReportingRoleIsCBC702Correction
    case "CBCErrorCode27a" => ReportingRoleIsCBC702Initial2
    case "CBCErrorCode27b" => ReportingRoleIsCBC702Correction2
    case "CBCErrorCode28"  => WhiteSpaceSchemaValidation
    case "CBCErrorCode29"  => BizActivitiesCBC513
    case "CBCErrorCode30"  => NegativeNumberOfEmployees
    case "CBCErrorCode31"  => Exceeding10mEmployees
    case "CBCErrorCode32"  => InitialMustContainOneCBCReport
    case "CBCErrorCode33"  => OECD0CorrDocRefMustNotBePresent
    case "CBCErrorCode34"  => MessageTypeIndicCBC402andOECD0
    case "CBCErrorCode35"  => MessageTypeIndicCBC401DocTypeOnlyOECD1
    case "CBCErrorCode36"  => SchemaVersionNumber
    case "CBCErrorCode37"  => TINProvidedIssuedByAttributeMustBeCompleted
    case "CBCErrorCode38"  => SendingEntityINIsAMandatoryField
    case "CBCErrorCode39a" => SpecificReportingRoleCantMatchSpecificConstEntityRole
    case "CBCErrorCode39b" => SpecificReportingRoleCantMatchSpecificConstEntityRole2
    case "CBCErrorCode40a" => SpecificReportingRoleCantMatchSpecificConstEntityRole3
    case "CBCErrorCode40b" => SpecificReportingRoleCantMatchSpecificConstEntityRole4
    case "CBCErrorCode42"  => ReportingRoleOECD0IsNotTheSame
    case "CBCErrorCode43"  => ReportingRoleOECD0IsNotTheSame2
    case "99999"           => CustomError
    case otherCode         => UnknownErrorCode(otherCode)
  }
}

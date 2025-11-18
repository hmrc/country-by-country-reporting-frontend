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
  case object CorrMessageRefIDForbiddenInDocSpec extends BusinessRuleErrorCode("80006")
  case object CorrMessageRefIDForbiddenInMessageHeader extends BusinessRuleErrorCode("80007")
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

  case class UnknownErrorCode(override val code: String) extends BusinessRuleErrorCode(code)

  val values: Seq[BusinessRuleErrorCode] = Seq(
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
    CorrMessageRefIDForbiddenInDocSpec,
    CorrMessageRefIDForbiddenInMessageHeader,
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
    ReportingRoleOECD0IsNotTheSame2
  )

  implicit val writes: Writes[BusinessRuleErrorCode] = Writes[BusinessRuleErrorCode] { x =>
    JsString(x.code)
  }

  implicit val reads: Reads[BusinessRuleErrorCode] = __.read[String].map {
    case "50008"              => InvalidMessageRefIDFormat
    case "50009"              => MessageRefIDHasAlreadyBeenUsed
    case "50010"              => FileContainsTestDataForProductionEnvironment
    case "50012"              => NotMeantToBeReceivedByTheIndicatedJurisdiction
    case "80000"              => DocRefIDAlreadyUsed
    case "80001"              => DocRefIDFormat
    case "80002"              => CorrDocRefIdUnknown
    case "80003"              => CorrDocRefIdNoLongerValid
    case "80004"              => CorrDocRefIdForNewData
    case "80005"              => MissingCorrDocRefId
    case "80006"              => CorrMessageRefIDForbiddenInDocSpec
    case "80007"              => CorrMessageRefIDForbiddenInMessageHeader
    case "80008"              => ResendOption
    case "80009"              => DeleteParentRecord
    case "80010"              => MessageTypeIndic
    case "80011"              => CorrDocRefIDTwiceInSameMessage
    case "80013"              => UnknownDocRefID
    case "80014"              => DocRefIDIsNoLongerValid
    case "CBC Error Code 1"   => MoreThanOneCBCBody
    case "CBC Error Code 2"   => MessageSpecLargerThanSendEntity
    case "CBC Error Code 3"   => DocRefIDMustMatchMessageRefID
    case "CBC Error Code 4"   => MessageRefIDFileNameMatch
    case "CBC Error Code 5"   => ReportingPeriodMatchYear
    case "CBC Error Code 6"   => CBCIDIncorrect
    case "CBC Error Code 7"   => MessageTypeIndicMatch
    case "CBC Error Code 8"   => XMLTimestampMatch
    case "CBC Error Code 9"   => TINMatch
    case "CBC Error Code 10"  => DocTypeIndicMatch
    case "CBC Error Code 11"  => XMLParentGroupMatch
    case "CBC Error Code 12"  => ReportingPeriodCantChange
    case "CBC Error Code 13"  => ReportingPeriodMatchEndDate
    case "CBC Error Code 14"  => MultipleInitialReportingPeriods
    case "CBC Error Code 15a" => PreviousSubmissionReportingTypeOverlap
    case "CBC Error Code 15b" => PreviousSubmissionReportingTypeOverlap2
    case "CBC Error Code 16"  => ReportingPeriodStartBeforeEnd
    case "CBC Error Code 17"  => OldestAllowableReportingPeriod
    case "CBC Error Code 18"  => FutureReportingPeriod
    case "CBC Error Code 20"  => ReportingEntityTINFormat
    case "CBC Error Code 21a" => ConstEntityMatchInitial
    case "CBC Error Code 21b" => ConstEntityMatchCorrection
    case "CBC Error Code 22a" => DuplicationOfConstEntity
    case "CBC Error Code 22b" => DuplicationOfConstEntity2
    case "CBC Error Code 23a" => CurrencyCodeInitial
    case "CBC Error Code 23b" => CurrencyCodeCorrection
    case "CBC Error Code 24a" => ConstEntitiesRoleInitial
    case "CBC Error Code 24b" => ConstEntitiesRoleCorrection
    case "CBC Error Code 25a" => ReportingRoleIsCBC701Initial
    case "CBC Error Code 25b" => ReportingRoleIsCBC701Correction
    case "CBC Error Code 26a" => ReportingRoleIsCBC702Initial
    case "CBC Error Code 26b" => ReportingRoleIsCBC702Correction
    case "CBC Error Code 27a" => ReportingRoleIsCBC702Initial2
    case "CBC Error Code 27b" => ReportingRoleIsCBC702Correction2
    case "CBC Error Code 28"  => WhiteSpaceSchemaValidation
    case "CBC Error Code 29"  => BizActivitiesCBC513
    case "CBC Error Code 30"  => NegativeNumberOfEmployees
    case "CBC Error Code 31"  => Exceeding10mEmployees
    case "CBC Error Code 32"  => InitialMustContainOneCBCReport
    case "CBC Error Code 33"  => OECD0CorrDocRefMustNotBePresent
    case "CBC Error Code 34"  => MessageTypeIndicCBC402andOECD0
    case "CBC Error Code 35"  => MessageTypeIndicCBC401DocTypeOnlyOECD1
    case "CBC Error Code 36"  => SchemaVersionNumber
    case "CBC Error Code 37"  => TINProvidedIssuedByAttributeMustBeCompleted
    case "CBC Error Code 38"  => SendingEntityINIsAMandatoryField
    case "CBC Error Code 39a" => SpecificReportingRoleCantMatchSpecificConstEntityRole
    case "CBC Error Code 39b" => SpecificReportingRoleCantMatchSpecificConstEntityRole2
    case "CBC Error Code 40a" => SpecificReportingRoleCantMatchSpecificConstEntityRole3
    case "CBC Error Code 40b" => SpecificReportingRoleCantMatchSpecificConstEntityRole4
    case "CBC Error Code 42"  => ReportingRoleOECD0IsNotTheSame
    case "CBC Error Code 43"  => ReportingRoleOECD0IsNotTheSame2
    case otherCode            => UnknownErrorCode(otherCode)
  }
}

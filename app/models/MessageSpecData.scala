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

import play.api.libs.json._

sealed trait MessageTypeIndic
case object CBC401 extends MessageTypeIndic
case object CBC402 extends MessageTypeIndic

object MessageTypeIndic {

  val values: Seq[MessageTypeIndic] = Seq(CBC401, CBC402)

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

case class MessageSpecData(messageRefId: String, messageTypeIndic: MessageTypeIndic, reportingEntityName: String, reportType: ReportType)

object MessageSpecData {
  implicit val format: OFormat[MessageSpecData] = Json.format[MessageSpecData]
}

case class ValidatedFileData(fileName: String, messageSpecData: MessageSpecData, fileSize: Long, checksum: String)

object ValidatedFileData {
  implicit val format: OFormat[ValidatedFileData] = Json.format[ValidatedFileData]
}

sealed trait ReportType
case object TestData extends ReportType
case object NewInformation extends ReportType
case object DeletionOfAllInformation extends ReportType
case object NewInformationForExistingReport extends ReportType
case object CorrectionForExistingReport extends ReportType
case object DeletionForExistingReport extends ReportType
case object CorrectionAndDeletionForExistingReport extends ReportType
case object CorrectionForReportingEntity extends ReportType

object ReportType {

  val values: Seq[ReportType] = Seq(
    TestData,
    NewInformation,
    DeletionOfAllInformation,
    NewInformationForExistingReport,
    CorrectionForExistingReport,
    DeletionForExistingReport,
    CorrectionAndDeletionForExistingReport,
    CorrectionForReportingEntity
  )

  implicit val writes: Writes[ReportType] = Writes[ReportType] {
    case TestData                               => JsString("TEST_DATA")
    case NewInformation                         => JsString("NEW_INFORMATION")
    case DeletionOfAllInformation               => JsString("DELETION_OF_ALL_INFORMATION")
    case NewInformationForExistingReport        => JsString("NEW_INFORMATION_FOR_EXISTING_REPORT")
    case CorrectionForExistingReport            => JsString("CORRECTION_FOR_EXISTING_REPORT")
    case DeletionForExistingReport              => JsString("DELETION_FOR_EXISTING_REPORT")
    case CorrectionAndDeletionForExistingReport => JsString("CORRECTION_AND_DELETION_FOR_EXISTING_REPORT")
    case CorrectionForReportingEntity           => JsString("CORRECTION_FOR_REPORTING_ENTITY")
  }

  implicit val reads: Reads[ReportType] = Reads[ReportType] {
    case JsString("TEST_DATA")                                   => JsSuccess(TestData)
    case JsString("NEW_INFORMATION")                             => JsSuccess(NewInformation)
    case JsString("DELETION_OF_ALL_INFORMATION")                 => JsSuccess(DeletionOfAllInformation)
    case JsString("NEW_INFORMATION_FOR_EXISTING_REPORT")         => JsSuccess(NewInformationForExistingReport)
    case JsString("CORRECTION_FOR_EXISTING_REPORT")              => JsSuccess(CorrectionForExistingReport)
    case JsString("DELETION_FOR_EXISTING_REPORT")                => JsSuccess(DeletionForExistingReport)
    case JsString("CORRECTION_AND_DELETION_FOR_EXISTING_REPORT") => JsSuccess(CorrectionAndDeletionForExistingReport)
    case JsString("CORRECTION_FOR_REPORTING_ENTITY")             => JsSuccess(CorrectionForReportingEntity)
    case value                                                   => JsError(s"Unexpected value of _type: $value")
  }
}

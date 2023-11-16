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

package viewmodels

import models.fileDetails._
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

object FileRejectedViewModel {

  def createTable(validationErrors: ValidationErrors)(implicit messages: Messages): Table = {

    val header = Some(
      Seq(
        HeadCell(Text(Messages("fileRejected.code"))),
        HeadCell(Text(Messages("fileRejected.docRefId")), classes = "govuk-!-width-one-half"),
        HeadCell(Text(Messages("fileRejected.error")), classes = "govuk-!-width-one-half")
      )
    )
    Table(
      rows = createTableRow(validationErrors),
      head = header,
      firstCellIsHeader = false,
      caption = Some(Messages("fileRejected.tableCaption")),
      captionClasses = "govuk-table__caption govuk-heading-m"
    )
  }

  private def docIdContent(docRefIds: Seq[String]): Html = Html(
    docRefIds
      .map(
        docRefId => s"<div class='govuk-!-padding-bottom-2 text-overflow'>$docRefId</div>"
      )
      .mkString(" ")
  )

  //noinspection ScalaStyle
  private def handleCustomErrors(errorDetails: Option[String], docRefIDInError: Option[Seq[String]])(implicit
    messages: Messages
  ): (String, HtmlContent, Content) =
    errorDetails.getOrElse("") match {
      case error if error.contains(`error_details_901`) =>
        ("901", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.901.value"))
      case error if error.contains(`error_details_902`) =>
        ("902", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.902.value"))
      case error if error.contains(`error_details_903`) =>
        ("903", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.903.value"))
      case error if error.contains(`error_details_904a`) =>
        ("904", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.904.value"))
      case error if error.contains(`error_details_904b`) =>
        ("904", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.904.value"))
      case error if error.contains(`error_details_904c`) =>
        ("904", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.904.value"))
      case error if error.contains(`error_details_904d`) =>
        ("904", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.904.value"))
      case error if error.contains(`error_details_905`) =>
        ("905", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.905.value"))
      case error if error.contains(`error_details_906`) =>
        ("906", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.906.value"))
      case error if error.contains(`error_details_907`) =>
        ("907", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.907.value"))
      case error if error.contains(`error_details_908`) =>
        ("908", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.908.value"))
      case error if error.contains(`error_details_909`) =>
        ("909", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.909.value"))
      case error if error.contains(`error_details_910`) => ("910", HtmlContent(Messages("label.file")), Messages(s"fileRejected.910.value"))
      case error if error.contains(`error_details_911`) =>
        ("911", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.911.value"))
      case error if error.contains(`error_details_912`) =>
        ("912", HtmlContent(docIdContent(docRefIDInError.getOrElse(Nil))), Messages(s"fileRejected.912.value"))
      case error => throw new Exception(s"The received RecordError details: $error is not the expected error details")
    }

  private def getDescriptionForRecordErrors(recordErrorCode: RecordErrorCode)(implicit messages: Messages): Content =
    recordErrorCode match {
      case RecordErrorCode.MessageTypeIndic =>
        HtmlContent(
          Html(
            s"${Messages(s"fileRejected.80010.value1")}" +
              s"<p>${Messages(s"fileRejected.80010.value2")}</p>" +
              s"<p>${Messages(s"fileRejected.80010.value3")}</p>" +
              s"<p class='remove-end-whitespace'>${Messages(s"fileRejected.80010.value4")}</p>"
          )
        )
      case errorCode =>
        Text(Messages(s"fileRejected.${errorCode.code}.value"))
    }

  private def createTableRow(validationErrors: ValidationErrors)(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val fileErrors: Option[Seq[(String, Content, Content)]] = validationErrors.fileError.map(
      _.map(
        error =>
          error.code match {
            case FileErrorCode.CustomError => handleCustomErrors(error.details, None)
            case errorCode =>
              (Messages(s"fileRejected.${errorCode.code}.key"), Text(Messages("label.file")), Text(Messages(s"fileRejected.${errorCode.code}.value")))
          }
      )
    )

    val recordErrors: Option[Seq[(String, Content, Content)]] = validationErrors.recordError.map(
      _.map(
        recordError =>
          recordError.code match {
            case RecordErrorCode.CustomError => handleCustomErrors(recordError.details, recordError.docRefIDInError)
            case errorCode =>
              val description: Content = getDescriptionForRecordErrors(errorCode)
              (Messages(s"fileRejected.${errorCode.code}.key"), HtmlContent(docIdContent(recordError.docRefIDInError.getOrElse(Nil))), description)
          }
      )
    )

    val errors: Seq[(String, Content, Content)] = (fileErrors ++ recordErrors).flatten.toSeq

    errors.map {
      case (code, docRefId, details) =>
        Seq(
          TableRow(code, attributes = Map("id" -> s"code_$code")),
          TableRow(docRefId, attributes = Map("id" -> s"docRefId_$code")),
          TableRow(details, attributes = Map("id" -> s"errorMessage_$code"))
        )
    }
  }

  //TODO: Update these messages when CBC business rules are known - These are the messages received from EIS for custom errors
  val error_details_901  = "CorrDocRefID element type does not match original DocRefID element type"
  val error_details_902  = "Correction is not for the relevant Disclosing element"
  val error_details_903  = "CorrDocRefID must not be present for OECD0 at Disclosing Element"
  val error_details_904a = "If the only Capacity entry is CBC501 the Reason must be either CBC701 or CBC901"
  val error_details_904b = "If the only Capacity entry is CBC502 the Reason must be either CBC702 or CBC902"
  val error_details_904c = "If the only Capacity entry is CBC503 the Reason must be either CBC701, CBC702, CBC901 or CBC902"
  val error_details_904d = "If the only Capacity entry is CBC504 then Reason must not be provided"
  val error_details_905  = "If DocTypeIndic of Disclosing is OECD0 the DocTypeIndic of CbcReport must OECD2"
  val error_details_906  = "If DocTypeIndic of CbcReport is OECD3 the DocTypeIndic of Disclosing must be OECD3"
  val error_details_907  = "If MessageTypeIndic is CBC401, Disclosing DocTypeIndic can only be OECD1"
  val error_details_908  = "If MessageTypeIndic is CBC401 and Diclosing DocTypeIndic is OECD1, the CbcReport must be present and have DocTypeIndic of OECD1"
  val error_details_909  = "DocRefID format does not match the format as set out in the HMRC CBC user guide"
  val error_details_910  = "MessageRefID format does not match the format as set out in the HMRC CBC user guide"
  val error_details_911  = """TIN issuedby must be provided where a TIN has been reported. The only exception is where "NOTIN" has been reported"""
  val error_details_912 =
    "The top level of the structure chart must not include the elements: cbc:ownership and cbc:InvestAmount. These should only be provided in the  cbc:ListChilds tag"

  val errorList: Seq[String] = Seq(
    error_details_901,
    error_details_902,
    error_details_903,
    error_details_904a,
    error_details_904b,
    error_details_904c,
    error_details_904d,
    error_details_905,
    error_details_906,
    error_details_907,
    error_details_908,
    error_details_909,
    error_details_911,
    error_details_912
  )
}

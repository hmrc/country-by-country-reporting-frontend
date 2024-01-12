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

package viewmodels

import controllers.routes
import models._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

object CheckYourFileDetailsViewModel {

  def getSummaryRows(vfd: ValidatedFileData)(implicit messages: Messages): Seq[SummaryListRow] = {
    val specData = vfd.messageSpecData
    Seq(
      SummaryListRowViewModel(
        key = "checkYourFileDetails.messageRefId",
        value = ValueViewModel(HtmlFormat.escape(s"${specData.messageRefId}").toString),
        actions = Seq()
      ),
      SummaryListRowViewModel(
        key = "checkYourFileDetails.reportType",
        value = ValueViewModel(
          HtmlFormat.escape(s"${getReportTypeContent(specData.reportType)}").toString
        ),
        actions = Seq(
          ActionItemViewModel(
            content = Text(messages("checkYourFileDetails.uploadedFile.change")),
            href = routes.UploadFileController.onPageLoad().url
          )
            .withAttribute(("id", "your-file"))
        )
      )
    )
  }

  def getAgentSummaryRows(validatedFileData: ValidatedFileData)(implicit messages: Messages): Seq[SummaryListRow] = {
    val fileDetails = getSummaryRows(validatedFileData)
    fileDetails.take(1) ++ getReportingEntityRow(validatedFileData.messageSpecData.reportingEntityName) ++ fileDetails.drop(1)
  }

  private def getReportingEntityRow(reportingEntityName: String)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    SummaryListRowViewModel(
      key = "checkYourFileDetails.reportingEntityName",
      value = ValueViewModel(HtmlFormat.escape(s"$reportingEntityName").toString),
      actions = Seq()
    )
  )

  private def getReportTypeContent(reportType: ReportType)(implicit messages: Messages) =
    messages(s"fileDetails.reportType.${reportType.toString}") //todo TEST cases

}

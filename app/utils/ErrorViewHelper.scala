/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import com.google.inject.Inject
import models.GenericError
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

class ErrorViewHelper @Inject() () {

  def generateTable(error: Seq[GenericError])(implicit messages: Messages): Seq[Seq[TableRow]] =
    error.map {
      er =>
        Seq(
          TableRow(content = Text(er.lineNumber.toString), classes = "govuk-table__cell--numeric", attributes = Map("id" -> s"lineNumber_${er.lineNumber}")),
          TableRow(content = Text(messages(er.message.messageKey, er.message.args: _*)), attributes = Map("id" -> s"errorMessage_${er.lineNumber}"))
        )
    }
}

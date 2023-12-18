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

package views.components

import base.SpecBase
import models.fileDetails.FileErrorCode._
import models.fileDetails.RecordErrorCode._
import models.fileDetails.{FileErrors, FileValidationErrors, RecordError, RecordErrorCode}
import play.api.i18n.{Messages, MessagesApi}
import views.html.components.FileRejectedTable

class FileRejectedTableSpec extends SpecBase {

  val view: FileRejectedTable  = app.injector.instanceOf[FileRejectedTable]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val messages: Messages       = messagesApi.preferred(Nil)

  "render a file error with a html error message" in {
    val fileError        = FileErrors(InvalidMessageRefIDFormat, None)
    val validationErrors = FileValidationErrors(Some(List(fileError)), None)

    view.render(validationErrors, messages).toString() must {
      include("<span>MessageRefId must be 100 characters or less and follow this structure in the order referenced:</span>") and
        include("<li>the same value as the Timestamp in the format ‘YYYYMMDDThhmmss’ or ‘YYYYMMDDThhmmssnnn’</li>")
    }
  }

  "render a file error with a standard error message" in {
    val fileError        = FileErrors(MessageRefIDHasAlreadyBeenUsed, None)
    val validationErrors = FileValidationErrors(Some(List(fileError)), None)

    view.render(validationErrors, messages).toString() must {
      include(messages("fileRejected.50009.value"))
    }
  }

  "render a record error with a doc ref id" in {
    val docRefId         = "doc reference 12345"
    val recordError      = RecordError(FileContainsTestDataForProductionEnvironment, None, Some(Seq(docRefId)))
    val validationErrors = FileValidationErrors(None, Some(Seq(recordError)))

    view.render(validationErrors, messages).toString() must {
      include(messages("fileRejected.50010.value")) and
        include(docRefId)
    }
  }

  "render a record error without a doc ref id" in {
    val recordError      = RecordError(DocRefIDAlreadyUsed, None, None)
    val validationErrors = FileValidationErrors(None, Some(List(recordError)))

    view.render(validationErrors, messages).toString() must {
      include(messages("fileRejected.80000.value"))
    }
  }

}

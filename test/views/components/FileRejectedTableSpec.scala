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
import play.api.i18n.{Messages, MessagesApi}
import viewmodels.FileRejectedError
import views.html.components.FileRejectedTable

class FileRejectedTableSpec extends SpecBase {

  val view: FileRejectedTable  = app.injector.instanceOf[FileRejectedTable]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val messages: Messages       = messagesApi.preferred(Nil)

  "render a error without a doc ref id" in {
    val errors = List(FileRejectedError(MessageRefIDHasAlreadyBeenUsed.code, Nil))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.50009.value"))
    }
  }

  "render a error with a doc ref id" in {
    val docRefId = "doc reference 12345"
    val errors   = List(FileRejectedError(FileContainsTestDataForProductionEnvironment.code, Seq(docRefId)))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.50010.value")) and
        include(docRefId)
    }
  }

  "render a error with a html error message" in {
    val errors = List(FileRejectedError(InvalidMessageRefIDFormat.code, Nil))

    view.render(errors, messages).toString() must {
      include("<span>MessageRefId must be 100 characters or less and follow this structure in the order referenced:</span>") and
        include("<li>the same value as the Timestamp in the format ‘YYYYMMDDThhmmss’ or ‘YYYYMMDDThhmmssnnn’</li>")
    }
  }

}

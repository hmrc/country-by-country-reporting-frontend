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
import models.fileDetails.FileErrorCode.FailedSchemaValidation
import models.fileDetails.{FileErrors, FileValidationErrors}
import play.api.i18n.{Messages, MessagesApi}
import views.html.components.FileRejectedTable

class FileRejectedTableSpec extends SpecBase {

  val view: FileRejectedTable  = app.injector.instanceOf[FileRejectedTable]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val messages: Messages       = messagesApi.preferred(Nil)

  "renders some html" in {
    val validationErrors = FileValidationErrors(None, None)

    view.render(validationErrors, messages).toString() must include("Hello")
  }

  "render a file error" in {
    val fileError        = FileErrors(FailedSchemaValidation, None)
    val validationErrors = FileValidationErrors(Some(List(fileError)), None)

    view.render(validationErrors, messages).toString() must {
      include("50007") and include("boom")
    }
  }

}

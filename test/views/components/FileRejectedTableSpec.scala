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

package views.components

import base.SpecBase
import models.fileDetails.BusinessRuleErrorCode._
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
      include(messages("fileRejected.50008.intro")) and
        include(messages("fileRejected.50008.bullet1"))
    }
  }

  "render a bulleted list error for error code 80001" in {
    val errors = List(FileRejectedError(MessageTypeIndic.code, Nil))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.80001.intro")) and
        include(messages("fileRejected.80001.bullet1"))
    }
  }

  "render a list of paragraphs error for error code 12" in {
    val errors = List(FileRejectedError(ReportingPeriodCantChange.code, Nil))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.12.p1")) and
        include(messages("fileRejected.12.p2"))
    }
  }

  "render a bulleted list error for error code 24" in {
    val errors = List(FileRejectedError(ConstEntitiesRoleInitial.code, Nil))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.24.intro")) and
        include(messages("fileRejected.24.bullet1"))
    }
  }

  "render a bulleted list error for 24b error code" in {
    val errors = List(FileRejectedError(ConstEntitiesRoleCorrection.code, Nil))

    view.render(errors, messages).toString() must {
      include(messages("fileRejected.24.intro")) and
        include(messages("fileRejected.24b.value"))
    }
  }

}

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

import models.fileDetails.{BusinessRuleErrorCode, FileErrors, FileValidationErrors, RecordError}
import org.scalatestplus.play.PlaySpec

class FileRejectedViewModelSpec extends PlaySpec {

  "FileRejectedViewModel" must {

    "return an empty list if there are no errors" in {
      val validationErrors = FileValidationErrors(None, None)
      val viewModel        = FileRejectedViewModel(validationErrors)

      val expected = Nil
      val actual   = viewModel.getErrors

      expected mustBe actual
    }

    "return a file error" in {
      val fileError        = FileErrors(BusinessRuleErrorCode.FailedSchemaValidation, None)
      val validationErrors = FileValidationErrors(Some(List(fileError)), None)
      val viewModel        = FileRejectedViewModel(validationErrors)

      val result = viewModel.getErrors

      result mustBe List(FileRejectedError(fileError.code.code, Nil))
    }

    "return a record error without a doc ref id" in {
      val recordError      = RecordError(BusinessRuleErrorCode.DocRefIDFormat, None, None)
      val validationErrors = FileValidationErrors(None, Some(List(recordError)))
      val viewModel        = FileRejectedViewModel(validationErrors)

      val result = viewModel.getErrors

      result mustBe List(FileRejectedError(recordError.code.code, Nil))
    }

    "return a record error with a doc ref id" in {
      val recordError      = RecordError(BusinessRuleErrorCode.DocRefIDFormat, None, Some(List("doc ref id 1")))
      val validationErrors = FileValidationErrors(None, Some(List(recordError)))
      val viewModel        = FileRejectedViewModel(validationErrors)

      val result = viewModel.getErrors

      result mustBe List(FileRejectedError(recordError.code.code, List("doc ref id 1")))
    }

  }

}

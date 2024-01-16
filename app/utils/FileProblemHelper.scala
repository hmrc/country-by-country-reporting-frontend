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

package utils

import models.fileDetails.BusinessRuleErrorCode._
import models.fileDetails._

object FileProblemHelper {

  private val expectedErrorCodes: Seq[String]       = BusinessRuleErrorCode.values.map(_.code)
  private val problemsStatusErrorCodes: Seq[String] = fileErrorCodesForProblemStatus.map(_.code) :+ DocRefIDFormat.code

  def isProblemStatus(errors: FileValidationErrors): Boolean = {
    val errorCodes: Seq[String] =
      Seq(errors.fileError.map(_.map(_.code.code)).getOrElse(Nil), errors.recordError.map(_.map(_.code.code)).getOrElse(Nil)).flatten

    (errorCodes.exists(
      !expectedErrorCodes.contains(_)
        || errorCodes.exists(problemsStatusErrorCodes.contains(_))
    )
    || recordErrorDetailNotAllowed(errors.recordError)
    || fileErrorDetailNotAllowed(errors.fileError))

  }

  private def recordErrorDetailNotAllowed(errors: Option[Seq[RecordError]]): Boolean =
    errors.exists(
      _.exists(
        error => error.code == CustomError
      )
    )

  private def fileErrorDetailNotAllowed(errors: Option[Seq[FileErrors]]): Boolean =
    errors.exists(
      _.exists(
        error => error.code == CustomError
      )
    )
}

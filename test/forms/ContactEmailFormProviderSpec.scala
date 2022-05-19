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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "contactEmail.error.required"
  val invalidKey  = "contactEmail.error.invalid"
  val lengthKey = "contactEmail.error.length"
  val maxLength = 131

  val form = new ContactEmailFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidString = "not a valid email",
      error = FormError(fieldName, invalidKey)
    )

    behave like fieldWithMaxLengthEmail(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

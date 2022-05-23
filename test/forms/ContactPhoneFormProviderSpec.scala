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

class ContactPhoneFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "contactPhone.error.required"
  val invalidKey  = "contactPhone.error.invalid"
  val lengthKey   = "contactPhone.error.length"
  val maxLength   = 24

  val form = new ContactPhoneFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPhoneNumberWithinLength(maxLength)
    )

    behave like fieldWithMaxLengthPhoneNumber(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq())
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      invalidString = "not a phone number",
      error = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

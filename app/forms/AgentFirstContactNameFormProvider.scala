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

package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import utils.RegExConstants

class AgentFirstContactNameFormProvider @Inject() extends Mappings with RegExConstants {

  private val maxLength = 35

  def apply(): Form[String] =
    Form(
      "value" -> validatedText("agentFirstContactName.error.required",
                               "agentFirstContactName.error.invalid",
                               "agentFirstContactName.error.length",
                               orgNameRegex,
                               maxLength
      )
    )
}

package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class CanWeContactByEmailFirstPageFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("canWeContactByEmailFirstPage.error.required")
    )
}

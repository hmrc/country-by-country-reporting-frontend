package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$className;format="decap"$.error.required")
    )
}

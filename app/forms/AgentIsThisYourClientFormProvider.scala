package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class AgentIsThisYourClientFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("agentIsThisYourClient.error.required")
    )
}

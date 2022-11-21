package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class AgentIsThisYourClientFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "agentIsThisYourClient.error.required"
  val invalidKey = "error.boolean"

  val form = new AgentIsThisYourClientFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

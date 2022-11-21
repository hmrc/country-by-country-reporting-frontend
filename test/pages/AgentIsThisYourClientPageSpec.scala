package pages

import pages.behaviours.PageBehaviours

class AgentIsThisYourClientPageSpec extends PageBehaviours {

  "AgentIsThisYourClientPage" - {

    beRetrievable[Boolean](AgentIsThisYourClientPage)

    beSettable[Boolean](AgentIsThisYourClientPage)

    beRemovable[Boolean](AgentIsThisYourClientPage)
  }
}

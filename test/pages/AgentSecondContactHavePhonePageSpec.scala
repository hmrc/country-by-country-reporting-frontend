package pages

import pages.behaviours.PageBehaviours

class AgentSecondContactHavePhonePageSpec extends PageBehaviours {

  "AgentSecondContactHavePhonePage" - {

    beRetrievable[Boolean](AgentSecondContactHavePhonePage)

    beSettable[Boolean](AgentSecondContactHavePhonePage)

    beRemovable[Boolean](AgentSecondContactHavePhonePage)
  }
}

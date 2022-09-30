package pages

import pages.behaviours.PageBehaviours

class CanWeContactByEmailFirstPagePageSpec extends PageBehaviours {

  "CanWeContactByEmailFirstPagePage" - {

    beRetrievable[Boolean](CanWeContactByEmailFirstPagePage)

    beSettable[Boolean](CanWeContactByEmailFirstPagePage)

    beRemovable[Boolean](CanWeContactByEmailFirstPagePage)
  }
}

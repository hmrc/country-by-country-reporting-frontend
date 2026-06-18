/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import base.SpecBase
import models.requests.DataRequest
import models.{ContactEmails, UserAnswers}
import pages.*
import play.api.i18n.Messages

class ContactHelperSpec extends SpecBase {

  private val app = applicationBuilder().build()

  implicit lazy val msgs: Messages = messages(app)

  private val helper = new ContactHelper {}

  "getContactEmails" - {
    "must return Some(ContactEmails) with both emails when both pages are answered" in {
      val ua = emptyUserAnswers
        .withPage(ContactEmailPage, "first@test.co.uk")
        .withPage(SecondContactEmailPage, "second@test.co.uk")

      implicit val request: DataRequest[_] = fakeDataRequest(ua)

      helper.getContactEmails() mustBe Some(ContactEmails("first@test.co.uk", Some("second@test.co.uk")))
    }

    "must return Some(ContactEmails) with None for second email when only first is answered" in {
      val ua = emptyUserAnswers
        .withPage(ContactEmailPage, "first@test.co.uk")

      implicit val request: DataRequest[_] = fakeDataRequest(ua)

      helper.getContactEmails() mustBe Some(ContactEmails("first@test.co.uk", None))
    }

    "must return None when ContactEmailPage is not answered" in {
      implicit val request: DataRequest[_] = fakeDataRequest(emptyUserAnswers)

      helper.getContactEmails() mustBe None
    }
  }

  "getAgentContactEmails" - {
    "must return Some(ContactEmails) with both emails when both agent pages are answered" in {
      val ua = emptyUserAnswers
        .withPage(AgentFirstContactEmailPage, "agent1@test.co.uk")
        .withPage(AgentSecondContactEmailPage, "agent2@test.co.uk")

      implicit val request: DataRequest[_] = fakeDataRequest(ua)

      helper.getAgentContactEmails() mustBe Some(ContactEmails("agent1@test.co.uk", Some("agent2@test.co.uk")))
    }

    "must return Some(ContactEmails) with None for second when only first agent email is answered" in {
      val ua = emptyUserAnswers
        .withPage(AgentFirstContactEmailPage, "agent1@test.co.uk")

      implicit val request: DataRequest[_] = fakeDataRequest(ua)

      helper.getAgentContactEmails() mustBe Some(ContactEmails("agent1@test.co.uk", None))
    }

    "must return None when AgentFirstContactEmailPage is not answered" in {
      implicit val request: DataRequest[_] = fakeDataRequest(emptyUserAnswers)

      helper.getAgentContactEmails() mustBe None
    }
  }

  "getFirstContactName" - {
    "must return the contact name when ContactNamePage is answered" in {
      val ua = emptyUserAnswers.withPage(ContactNamePage, "Test")

      helper.getFirstContactName(ua) mustBe "Test"
    }

    "must return the default message when ContactNamePage is not answered" in {
      helper.getFirstContactName(emptyUserAnswers) mustBe "your first contact"
    }
  }

  "getSecondContactName" - {
    "must return the contact name when SecondContactNamePage is answered" in {
      val ua = emptyUserAnswers.withPage(SecondContactNamePage, "Test")

      helper.getSecondContactName(ua) mustBe "Test"
    }

    "must return the default message when SecondContactNamePage is not answered" in {
      helper.getSecondContactName(emptyUserAnswers) mustBe "your second contact"
    }
  }

  "getPluralFirstContactName" - {
    "must return the plural.withS form when the name ends in 's'" in {
      val ua = emptyUserAnswers.withPage(ContactNamePage, "Testos")

      helper.getPluralFirstContactName(ua) mustBe "Testos’"
    }

    "must return the plain plural form when the name does not end in 's'" in {
      val ua = emptyUserAnswers.withPage(ContactNamePage, "Test")

      helper.getPluralFirstContactName(ua) mustBe "Test’s"
    }

    "must return the default plural form when ContactNamePage is not answered" in {
      helper.getPluralFirstContactName(emptyUserAnswers) mustBe "your first contact’s"
    }
  }

  "getPluralSecondContactName" - {
    "must return the plural.withS form when the name ends in 's'" in {
      val ua = emptyUserAnswers.withPage(SecondContactNamePage, "Testos")

      helper.getPluralSecondContactName(ua) mustBe "Testos’"
    }

    "must return the plain plural form when the name does not end in 's'" in {
      val ua = emptyUserAnswers.withPage(SecondContactNamePage, "Test")

      helper.getPluralSecondContactName(ua) mustBe "Test’s"
    }

    "must return the default plural form when SecondContactNamePage is not answered" in {
      helper.getPluralSecondContactName(emptyUserAnswers) mustBe "your second contact’s"
    }
  }

  "getAgentFirstContactName" - {
    "must return the agent contact name when AgentFirstContactNamePage is answered" in {
      val ua = emptyUserAnswers.withPage(AgentFirstContactNamePage, "AgentTestos")

      helper.getAgentFirstContactName(ua) mustBe "AgentTestos"
    }

    "must return the default message when AgentFirstContactNamePage is not answered" in {
      helper.getAgentFirstContactName(emptyUserAnswers) mustBe "your first contact"
    }
  }

  "getAgentSecondContactName" - {
    "must return the agent contact name when AgentSecondContactNamePage is answered" in {
      val ua = emptyUserAnswers.withPage(AgentSecondContactNamePage, "AgentTestos")

      helper.getAgentSecondContactName(ua) mustBe "AgentTestos"
    }

    "must return the default message when AgentSecondContactNamePage is not answered" in {
      helper.getAgentSecondContactName(emptyUserAnswers) mustBe "your second contact"
    }
  }

  "getPluralAgentFirstContactName" - {
    "must return the plural.withS form when the agent name ends in 's'" in {
      val ua = emptyUserAnswers.withPage(AgentFirstContactNamePage, "Testos")

      helper.getPluralAgentFirstContactName(ua) mustBe "Testos’"
    }

    "must return the plain plural form when the agent name does not end in 's'" in {
      val ua = emptyUserAnswers.withPage(AgentFirstContactNamePage, "Test")

      helper.getPluralAgentFirstContactName(ua) mustBe "Test’s"
    }

    "must return the default plural form when AgentFirstContactNamePage is not answered" in {
      helper.getPluralAgentFirstContactName(emptyUserAnswers) mustBe "your first contact’s"
    }
  }

  "getPluralAgentSecondContactName" - {
    "must return the plural.withS form when the agent name ends in 's'" in {
      val ua = emptyUserAnswers.withPage(AgentSecondContactNamePage, "Testos")

      helper.getPluralAgentSecondContactName(ua) mustBe "Testos’"
    }

    "must return the plain plural form when the agent name does not end in 's'" in {
      val ua = emptyUserAnswers.withPage(AgentSecondContactNamePage, "Test")

      helper.getPluralAgentSecondContactName(ua) mustBe "Test’s"
    }

    "must return the default plural form when AgentSecondContactNamePage is not answered" in {
      helper.getPluralAgentSecondContactName(emptyUserAnswers) mustBe "your second contact’s"
    }
  }

  private def fakeDataRequest(ua: UserAnswers): DataRequest[_] = {
    val req = mock[DataRequest[_]]
    when(req.userAnswers).thenReturn(ua)
    req
  }
}

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

package utils

import models.{ContactEmails, UserAnswers}
import models.requests.DataRequest
import pages.{AgentFirstContactNamePage, AgentSecondContactNamePage, ContactEmailPage, ContactNamePage, SecondContactEmailPage, SecondContactNamePage}
import play.api.i18n.Messages

trait ContactHelper {

  def getContactEmails()(implicit request: DataRequest[_]): Option[ContactEmails] =
    request.userAnswers.get(ContactEmailPage) map {
      firstContactEmail =>
        ContactEmails(firstContactEmail, request.userAnswers.get(SecondContactEmailPage))
    }

  def getFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(ContactNamePage)
      .fold(messages("default.firstContact.name"))(
        contactName => contactName
      )

  def getSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(SecondContactNamePage)
      .fold(messages("default.secondContact.name"))(
        contactName => contactName
      )

  def getPluralFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(ContactNamePage)
      .fold(messages("contact.name.plural", messages("default.firstContact.name")))(
        contactName =>
          if (contactName.endsWith("s")) {
            messages("contact.name.plural.withS", contactName)
          } else {
            messages("contact.name.plural", contactName)
          }
      )

  def getPluralSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(SecondContactNamePage)
      .fold(messages("contact.name.plural", messages("default.secondContact.name")))(
        contactName =>
          if (contactName.endsWith("s")) {
            messages("contact.name.plural.withS", contactName)
          } else {
            messages("contact.name.plural", contactName)
          }
      )

  def getAgentFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(AgentFirstContactNamePage)
      .fold(messages("default.firstContact.name"))(
        contactName => contactName
      )

  def getAgentSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(AgentSecondContactNamePage)
      .fold(messages("default.secondContact.name"))(
        contactName => contactName
      )

  def getPluralAgentFirstContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(AgentFirstContactNamePage)
      .fold(messages("contact.name.plural", messages("default.firstContact.name")))(
        contactName =>
          if (contactName.endsWith("s")) {
            messages("contact.name.plural.withS", contactName)
          } else {
            messages("contact.name.plural", contactName)
          }
      )

  def getPluralAgentSecondContactName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers
      .get(AgentSecondContactNamePage)
      .fold(messages("contact.name.plural", messages("default.secondContact.name")))(
        contactName =>
          if (contactName.endsWith("s")) {
            messages("contact.name.plural.withS", contactName)
          } else {
            messages("contact.name.plural", contactName)
          }
      )
}

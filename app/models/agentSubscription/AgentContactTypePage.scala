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

package models.agentSubscription

import pages._

sealed trait AgentContactTypePage {
  def contactNamePage: QuestionPage[String]
  def contactEmailPage: QuestionPage[String]
  def contactTelephonePage: QuestionPage[String]
  def haveTelephonePage: QuestionPage[Boolean]
}

case class PrimaryAgentContactDetailsPages(contactNamePage: QuestionPage[String],
                                           contactEmailPage: QuestionPage[String],
                                           contactTelephonePage: QuestionPage[String],
                                           haveTelephonePage: QuestionPage[Boolean]
) extends AgentContactTypePage

case class SecondaryAgentContactDetailsPages(contactNamePage: QuestionPage[String],
                                             contactEmailPage: QuestionPage[String],
                                             contactTelephonePage: QuestionPage[String],
                                             haveTelephonePage: QuestionPage[Boolean]
) extends AgentContactTypePage

object AgentContactTypePage {

  implicit val primaryAgentContactDetailsPages: PrimaryAgentContactDetailsPages =
    PrimaryAgentContactDetailsPages(AgentFirstContactNamePage, AgentFirstContactEmailPage, AgentFirstContactPhonePage, AgentFirstContactHavePhonePage)

  implicit val secondaryAgentContactDetailsPages: SecondaryAgentContactDetailsPages =
    SecondaryAgentContactDetailsPages(AgentSecondContactNamePage, AgentSecondContactEmailPage, AgentSecondContactPhonePage, AgentSecondContactHavePhonePage)

}

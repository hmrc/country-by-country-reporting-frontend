/*
 * Copyright 2023 HM Revenue & Customs
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

import models.UserAnswers
import pages.{AgentHaveSecondContactPage, AgentSecondContactNamePage}
import play.api.libs.json.{Json, Writes}

case class AgentRequestDetailForUpdate(IDType: String,
                                       IDNumber: String,
                                       tradingName: Option[String],
                                       isGBUser: Boolean,
                                       primaryContact: AgentContactInformation,
                                       secondaryContact: Option[AgentContactInformation]
)

object AgentRequestDetailForUpdate {
  implicit lazy val writes: Writes[AgentRequestDetailForUpdate] = Json.writes[AgentRequestDetailForUpdate]

  def convertToRequestDetails(agentResponseDetail: AgentResponseDetail, userAnswers: UserAnswers): Option[AgentRequestDetailForUpdate] = {
    val primaryContact =
      getAgentContactInformation[PrimaryAgentContactDetailsPages](agentResponseDetail.primaryContact.agentDetails,
                                                                  agentResponseDetail.primaryContact.mobile,
                                                                  userAnswers
      )

    val secondaryContact =
      (userAnswers.get(AgentHaveSecondContactPage), agentResponseDetail.secondaryContact, userAnswers.get(AgentSecondContactNamePage)) match {
        case (Some(true), _, Some(orgName)) => getAgentContactInformation[SecondaryAgentContactDetailsPages](AgentDetails(orgName), None, userAnswers)
        case (Some(true), Some(contactInformation), _) =>
          getAgentContactInformation[SecondaryAgentContactDetailsPages](contactInformation.agentDetails, contactInformation.mobile, userAnswers)
        case _ => None
      }

    primaryContact map {
      primaryContact =>
        AgentRequestDetailForUpdate("ARN",
                                    agentResponseDetail.subscriptionID,
                                    agentResponseDetail.tradingName,
                                    agentResponseDetail.isGBUser,
                                    primaryContact,
                                    secondaryContact
        )
    }
  }

  def getAgentContactInformation[T <: AgentContactTypePage](contactInfo: AgentDetails, mobile: Option[String], userAnswers: UserAnswers)(implicit
    agentContactTypePage: T
  ): Option[AgentContactInformation] = {

    val contactTypeInfo = userAnswers.get(agentContactTypePage.contactNamePage) match {
      case Some(orgName) => AgentDetails(orgName)
      case _             => contactInfo
    }

    for {
      email               <- userAnswers.get(agentContactTypePage.contactEmailPage)
      haveTelephoneNumber <- userAnswers.get(agentContactTypePage.haveTelephonePage)
    } yield {
      val phoneNumber = if (haveTelephoneNumber) userAnswers.get(agentContactTypePage.contactTelephonePage) else None
      AgentContactInformation(contactTypeInfo, email, phoneNumber, mobile)
    }
  }

}

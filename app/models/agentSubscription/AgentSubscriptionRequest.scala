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

import models.{ApiError, SubscriptionCreateInformationMissingError, UserAnswers}
import models.agentSubscription.AgentRequestCommonForSubscription.createAgentRequestCommonForSubscription
import pages._
import play.api.Logging
import play.api.libs.json.{Json, OFormat}

case class AgentSubscriptionRequest(
  requestCommon: AgentRequestCommonForSubscription,
  requestDetail: AgentRequestDetail
)

object AgentSubscriptionRequest extends Logging {
  private val idType: String                             = "ARN"
  implicit val format: OFormat[AgentSubscriptionRequest] = Json.format[AgentSubscriptionRequest]

  def createAgentSubscriptionRequest(arn: String, userAnswers: UserAnswers): Either[ApiError, AgentSubscriptionRequest] =
    getPrimaryContactInformation(userAnswers) match {
      case Some(pc) =>
        getSecondaryContactInformation(userAnswers) match {
          case Right(sc) =>
            Right(
              AgentSubscriptionRequest(
                createAgentRequestCommonForSubscription(),
                AgentRequestDetail(
                  IDType = idType,
                  IDNumber = arn,
                  tradingName = None,
                  isGBUser = true,
                  primaryContact = pc,
                  secondaryContact = sc
                )
              )
            )
          case Left(error) =>
            logger.warn(s"createAgentSubscriptionRequest failed: $error")
            Left(error)
        }
      case _ =>
        logger.warn("createAgentSubscriptionRequest failed due to missing agent contact details")
        Left(SubscriptionCreateInformationMissingError("Primary AgentContactInformation"))
    }

  def getPrimaryContactInformation(userAnswers: UserAnswers): Option[AgentContactInformation] =
    for {
      agentEmail       <- userAnswers.get(AgentFirstContactEmailPage)
      agentContactInfo <- userAnswers.get(AgentFirstContactNamePage).map(AgentDetails(_))
    } yield AgentContactInformation(agentContactInfo, agentEmail, userAnswers.get(ContactPhonePage), None)

  def getSecondaryContactInformation(userAnswers: UserAnswers): Either[ApiError, Option[AgentContactInformation]] = {
    val doYouHaveSecondContact      = userAnswers.get(AgentHaveSecondContactPage)
    val doYouHaveSecondContactPhone = userAnswers.get(AgentSecondContactHavePhonePage)
    (doYouHaveSecondContact, doYouHaveSecondContactPhone) match {
      case (Some(false), _) => Right(None)
      case (Some(true), Some(_)) =>
        Right(
          for {
            agentSecondaryContactInfo <- userAnswers.get(SecondContactNamePage).map(AgentDetails(_))
            agentSecondaryEmail       <- userAnswers.get(SecondContactEmailPage)
          } yield AgentContactInformation(agentSecondaryContactInfo, agentSecondaryEmail, userAnswers.get(SecondContactPhonePage), None)
        )
      case (Some(true), None) => Left(SubscriptionCreateInformationMissingError("AgentSecondContactHavePhone Page not answered"))
      case (None, _)          => Left(SubscriptionCreateInformationMissingError("AgentHaveSecondContact Page not answered"))
    }

  }

}

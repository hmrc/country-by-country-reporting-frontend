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

package services

import connectors.AgentSubscriptionConnector
import models.UserAnswers
import models.agentSubscription._
import pages.{AgentHaveSecondContactPage, AgentSecondContactNamePage}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSubscriptionService @Inject() (agentSubscriptionConnector: AgentSubscriptionConnector)(implicit ec: ExecutionContext) extends Logging {

  def getAgentContactDetails(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    agentSubscriptionConnector.readSubscription map {
      responseOpt =>
        responseOpt.fold {
          Option(userAnswers)
        } {
          responseDetail => populateUserAnswers(responseDetail, userAnswers)
        }
    }

  def updateAgentContactDetails(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] =
    agentSubscriptionConnector.readSubscription flatMap {
      case Some(agentResponseDetails) =>
        AgentRequestDetailForUpdate.convertToRequestDetails(agentResponseDetails, userAnswers) match {
          case Some(agentRequestDetails) => agentSubscriptionConnector.updateSubscription(agentRequestDetails)
          case _ =>
            logger.warn("updateAgentContactDetails: failed to convert userAnswers to AgentRequestDetailForUpdate")
            Future.successful(false)
        }
      case _ =>
        logger.warn("updateAgentContactDetails: readSubscription call failed to fetch the data")
        Future.successful(false)
    }

  def doAgentContactDetailsExist()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    agentSubscriptionConnector.checkSubscriptionExists

  def isAgentContactInformationUpdated(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    agentSubscriptionConnector.readSubscription map {
      case Some(responseDetail) =>
        val secondaryContact =
          (userAnswers.get(AgentHaveSecondContactPage), responseDetail.secondaryContact, userAnswers.get(AgentSecondContactNamePage)) match {
            case (Some(true), _, Some(orgName)) => populateResponseDetails[SecondaryAgentContactDetailsPages](userAnswers, AgentDetails(orgName), None)
            case (Some(true), Some(agentContactInformation), _) =>
              populateResponseDetails[SecondaryAgentContactDetailsPages](userAnswers, agentContactInformation.agentDetails, agentContactInformation.mobile)
            case _ => None
          }

        for {
          primaryContact <- populateResponseDetails[PrimaryAgentContactDetailsPages](userAnswers,
                                                                                     responseDetail.primaryContact.agentDetails,
                                                                                     responseDetail.primaryContact.mobile
          )
        } yield !responseDetail.copy(primaryContact = primaryContact, secondaryContact = secondaryContact).equals(responseDetail)

      case _ =>
        logger.warn("isContactInformationUpdated: readSubscription call failed to fetch the data")
        None
    }

  private def populateResponseDetails[T <: AgentContactTypePage](userAnswers: UserAnswers, contactInfo: AgentDetails, mobile: Option[String])(implicit
    contactTypePage: T
  ): Option[AgentContactInformation] = {

    val updatedContactType = userAnswers.get(contactTypePage.contactNamePage) match {
      case Some(orgName) => AgentDetails(orgName)
      case _             => contactInfo
    }

    for {
      email               <- userAnswers.get(contactTypePage.contactEmailPage)
      haveTelephoneNumber <- userAnswers.get(contactTypePage.haveTelephonePage)
    } yield {
      val contactTelephone = if (haveTelephoneNumber) userAnswers.get(contactTypePage.contactTelephonePage) else None
      AgentContactInformation(updatedContactType, email, contactTelephone, mobile)
    }

  }

  private def populateUserAnswers(responseDetail: AgentResponseDetail, userAnswers: UserAnswers): Option[UserAnswers] =
    populateContactInfo[PrimaryAgentContactDetailsPages](userAnswers, responseDetail.primaryContact, isSecondaryContact = false) map {
      uaWithPrimaryContact =>
        responseDetail.secondaryContact
          .flatMap {
            sc => populateContactInfo[SecondaryAgentContactDetailsPages](uaWithPrimaryContact, sc, isSecondaryContact = true)
          }
          .getOrElse(uaWithPrimaryContact)
    }

  private def populateContactInfo[T <: AgentContactTypePage](userAnswers: UserAnswers,
                                                             contactInformation: AgentContactInformation,
                                                             isSecondaryContact: Boolean
  )(implicit
    contactTypePage: T
  ): Option[UserAnswers] =
    (for {
      uaWithSecondContact <- userAnswers.set(AgentHaveSecondContactPage, isSecondaryContact)
      uaWithEmail         <- uaWithSecondContact.set(contactTypePage.contactEmailPage, contactInformation.email)
      uaWithTelephone     <- uaWithEmail.set(contactTypePage.contactTelephonePage, contactInformation.phone.getOrElse(""))
      uaWithHaveTelephone <- uaWithTelephone.set(contactTypePage.haveTelephonePage, contactInformation.phone.exists(_.nonEmpty))
      updatedAnswers      <- uaWithHaveTelephone.set(contactTypePage.contactNamePage, contactInformation.agentDetails.organisationName)
    } yield updatedAnswers).toOption

}

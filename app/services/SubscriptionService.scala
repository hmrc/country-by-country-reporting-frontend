/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.SubscriptionConnector
import models.UserAnswers
import models.subscription._
import pages.{HaveSecondContactPage, PrimaryClientContactInformationPage, SecondContactNamePage}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext)
    extends Logging {

  val migratedUserEmail = appConfig.migratedUserEmail.toLowerCase
  val migratedUserName  = appConfig.migratedUserName.toLowerCase

  private def isUserVisitingAfterMigration(responseDetail: ResponseDetail): Boolean =
    responseDetail.secondaryContact.isDefined &&
      (responseDetail.secondaryContact.get.email.toLowerCase.contains(migratedUserEmail) &&
        responseDetail.secondaryContact.get.organisationDetails.organisationName.toLowerCase.contains(migratedUserName))

  def getContactDetails(userAnswers: UserAnswers, subscriptionId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    subscriptionConnector.readSubscription(subscriptionId: String) map {
      responseOpt =>
        responseOpt.flatMap {
          responseDetail =>
            if (isUserVisitingAfterMigration(responseDetail)) {
              storePrimaryContactInfo(userAnswers, responseDetail)
            } else {
              populateUserAnswers(responseDetail, userAnswers)
            }
        }
    }

  private def storePrimaryContactInfo(userAnswers: UserAnswers, responseDetail: ResponseDetail): Option[UserAnswers] = (for {
    updatedAnswers <- userAnswers.set(PrimaryClientContactInformationPage, responseDetail.primaryContact)
  } yield updatedAnswers).toOption

  def updateContactDetails(userAnswers: UserAnswers, subscriptionId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    subscriptionConnector.readSubscription(subscriptionId: String) flatMap {
      case Some(responseDetails) =>
        RequestDetailForUpdate.convertToRequestDetails(responseDetails, userAnswers) match {
          case Some(requestDetails) => subscriptionConnector.updateSubscription(requestDetails)
          case _ =>
            logger.warn("updateContactDetails: failed to convert userAnswers to RequestDetailForUpdate")
            Future.successful(false)
        }
      case _ =>
        logger.warn("updateContactDetails: readSubscription call failed to fetch the data")
        Future.successful(false)
    }

  def isContactInformationUpdated(userAnswers: UserAnswers, subscriptionId: String)(implicit hc: HeaderCarrier): Future[Option[(Boolean, Boolean)]] =
    subscriptionConnector.readSubscription(subscriptionId: String) map {
      case Some(responseDetail) =>
        val secondaryContact = (userAnswers.get(HaveSecondContactPage), responseDetail.secondaryContact, userAnswers.get(SecondContactNamePage)) match {
          case (Some(true), _, Some(orgName)) => populateResponseDetails[SecondaryContactDetailsPages](userAnswers, OrganisationDetails(orgName), None)
          case (Some(true), Some(contactInformation), _) =>
            populateResponseDetails[SecondaryContactDetailsPages](userAnswers, contactInformation.organisationDetails, contactInformation.mobile)
          case _ => None
        }

        for {
          primaryContact <- populateResponseDetails[PrimaryContactDetailsPages](userAnswers,
                                                                                responseDetail.primaryContact.organisationDetails,
                                                                                responseDetail.primaryContact.mobile
          )
        } yield (!responseDetail.copy(primaryContact = primaryContact, secondaryContact = secondaryContact).equals(responseDetail),
                 isUserVisitingAfterMigration(responseDetail)
        )

      case _ =>
        logger.warn("isContactInformationUpdated: readSubscription call failed to fetch the data")
        None
    }

  def doContactDetailsExist(subscriptionId: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    subscriptionConnector.readSubscription(subscriptionId: String) map {
      case Some(responseDetail) => Some(!isUserVisitingAfterMigration(responseDetail))
      case _ =>
        logger.warn("isContactInformationUpdated: readSubscription call failed to fetch the data")
        None
    }

  private def populateResponseDetails[T <: ContactTypePage](userAnswers: UserAnswers, contactInfo: OrganisationDetails, mobile: Option[String])(implicit
    contactTypePage: T
  ): Option[ContactInformation] = {

    val updatedContactType = userAnswers.get(contactTypePage.contactNamePage) match {
      case Some(orgName) => OrganisationDetails(orgName)
      case _             => contactInfo
    }

    for {
      email               <- userAnswers.get(contactTypePage.contactEmailPage)
      haveTelephoneNumber <- userAnswers.get(contactTypePage.haveTelephonePage)
    } yield {
      val contactTelephone = if (haveTelephoneNumber) userAnswers.get(contactTypePage.contactTelephonePage) else None
      ContactInformation(updatedContactType, email, contactTelephone, mobile)
    }

  }

  private def populateUserAnswers(responseDetail: ResponseDetail, userAnswers: UserAnswers): Option[UserAnswers] =
    populateContactInfo[PrimaryContactDetailsPages](userAnswers, responseDetail.primaryContact, isSecondaryContact = false) map {
      uaWithPrimaryContact =>
        responseDetail.secondaryContact
          .flatMap {
            sc => populateContactInfo[SecondaryContactDetailsPages](uaWithPrimaryContact, sc, isSecondaryContact = true)
          }
          .getOrElse(uaWithPrimaryContact)
    }

  private def populateContactInfo[T <: ContactTypePage](userAnswers: UserAnswers, contactInformation: ContactInformation, isSecondaryContact: Boolean)(implicit
    contactTypePage: T
  ): Option[UserAnswers] =
    (for {
      uaWithSecondContact <- userAnswers.set(HaveSecondContactPage, isSecondaryContact)
      uaWithEmail         <- uaWithSecondContact.set(contactTypePage.contactEmailPage, contactInformation.email)
      uaWithTelephone     <- uaWithEmail.set(contactTypePage.contactTelephonePage, contactInformation.phone.getOrElse(""))
      uaWithHaveTelephone <- uaWithTelephone.set(contactTypePage.haveTelephonePage, contactInformation.phone.exists(_.nonEmpty))
      updatedAnswers      <- uaWithHaveTelephone.set(contactTypePage.contactNamePage, contactInformation.organisationDetails.organisationName)
    } yield updatedAnswers).toOption

  def getTradingName(subscriptionId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    subscriptionConnector.readSubscription(subscriptionId: String) map {
      case Some(responseDetail: ResponseDetail) => responseDetail.tradingName
      case _                                    => None
    }
}

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

package models.subscription

import models.UserAnswers
import pages.{HaveSecondContactPage, SecondContactNamePage}
import play.api.libs.json._

case class RequestDetailForUpdate(IDType: String,
                                  IDNumber: String,
                                  tradingName: Option[String],
                                  isGBUser: Boolean,
                                  primaryContact: ContactInformation,
                                  secondaryContact: Option[ContactInformation]
)

object RequestDetailForUpdate {
  implicit lazy val writes: Writes[RequestDetailForUpdate] = Json.writes[RequestDetailForUpdate]

  def convertToRequestDetails(responseDetail: ResponseDetail, userAnswers: UserAnswers): Option[RequestDetailForUpdate] = {
    val primaryContact =
      getContactInformation[PrimaryContactDetailsPages](responseDetail.primaryContact.organisationDetails, responseDetail.primaryContact.mobile, userAnswers)

    val secondaryContact = (userAnswers.get(HaveSecondContactPage), responseDetail.secondaryContact, userAnswers.get(SecondContactNamePage)) match {
      case (Some(true), _, Some(orgName)) => getContactInformation[SecondaryContactDetailsPages](OrganisationDetails(orgName), None, userAnswers)
      case (Some(true), Some(contactInformation), _) =>
        getContactInformation[SecondaryContactDetailsPages](contactInformation.organisationDetails, contactInformation.mobile, userAnswers)
      case _ => None
    }

    primaryContact map {
      primaryContact =>
        RequestDetailForUpdate("CBC", responseDetail.subscriptionID, responseDetail.tradingName, responseDetail.isGBUser, primaryContact, secondaryContact)
    }
  }

  def getContactInformation[T <: ContactTypePage](contactInfo: OrganisationDetails, mobile: Option[String], userAnswers: UserAnswers)(implicit
    contactTypePage: T
  ): Option[ContactInformation] = {

    val contactTypeInfo = userAnswers.get(contactTypePage.contactNamePage) match {
      case Some(orgName) => OrganisationDetails(orgName)
      case _             => contactInfo
    }

    for {
      email               <- userAnswers.get(contactTypePage.contactEmailPage)
      haveTelephoneNumber <- userAnswers.get(contactTypePage.haveTelephonePage)
    } yield {
      val phoneNumber = if (haveTelephoneNumber) userAnswers.get(contactTypePage.contactTelephonePage) else None
      ContactInformation(contactTypeInfo, email, phoneNumber, mobile)
    }
  }

}

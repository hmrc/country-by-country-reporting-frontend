/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.client

import models.UserAnswers
import models.subscription.ContactTypePage
import pages.PrimaryClientContactInformationPage
import play.api.Logging

import scala.util.{Failure, Success}

trait ReviewDetailsHelper extends Logging {

  def populateUserAnswers(userAnswers: UserAnswers, detailsAreCorrect: Boolean): UserAnswers =
    if (detailsAreCorrect) populateContactDetails(userAnswers) else unPopulateContactDetails(userAnswers)

  private def populateContactDetails(userAnswers: UserAnswers): UserAnswers = userAnswers.get(PrimaryClientContactInformationPage) match {
    case None => userAnswers
    case Some(contactInformation) =>
      (for {
        uaWithEmail         <- userAnswers.set(ContactTypePage.primaryContactDetailsPages.contactEmailPage, contactInformation.email)
        uaWithTelephone     <- uaWithEmail.set(ContactTypePage.primaryContactDetailsPages.contactTelephonePage, contactInformation.phone.getOrElse(""))
        uaWithHaveTelephone <- uaWithTelephone.set(ContactTypePage.primaryContactDetailsPages.haveTelephonePage, contactInformation.phone.exists(_.nonEmpty))
        updatedAnswers <- uaWithHaveTelephone.set(ContactTypePage.primaryContactDetailsPages.contactNamePage,
                                                  contactInformation.organisationDetails.organisationName
        )
      } yield updatedAnswers).getOrElse(userAnswers)
  }

  private def unPopulateContactDetails(userAnswers: UserAnswers): UserAnswers = userAnswers
    .remove(ContactTypePage.primaryContactDetailsPages.contactNamePage)
    .flatMap(_.remove(ContactTypePage.primaryContactDetailsPages.contactTelephonePage))
    .flatMap(_.remove(ContactTypePage.primaryContactDetailsPages.haveTelephonePage))
    .flatMap(_.remove(ContactTypePage.primaryContactDetailsPages.contactEmailPage)) match {
    case Success(ua) => ua
    case Failure(exception) =>
      logger.warn(s"Could not remove contact details ${exception.getMessage}")
      userAnswers
  }
}

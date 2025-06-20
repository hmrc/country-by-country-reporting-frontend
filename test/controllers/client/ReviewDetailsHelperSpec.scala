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

package controllers.client;

import base.SpecBase
import models.subscription.{ContactInformation, ContactTypePage, OrganisationDetails}
import org.scalatest.EitherValues
import pages.PrimaryClientContactInformationPage

class ReviewDetailsHelperSpec extends SpecBase with EitherValues {

  private val contactEmail     = "test@example.com"
  private val contactPhone     = Some("0123456789")
  private val organisationName = "Test Org"

  private val contactInfo = ContactInformation(
    organisationDetails = OrganisationDetails(organisationName),
    email = contactEmail,
    phone = contactPhone,
    mobile = None
  )
  val sut = new ReviewDetailsHelper {}

  "populateUserAnswers" - {

    "populate contact details when details are correct and contact info exists" in {
      val userAnswers = emptyUserAnswers.set(PrimaryClientContactInformationPage, contactInfo).success.value

      val updatedAnswers = sut.populateUserAnswers(userAnswers, detailsAreCorrect = true)

      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactEmailPage).value mustBe contactEmail
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactTelephonePage).value mustBe contactPhone.get
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.haveTelephonePage).value mustBe true
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactNamePage).value mustBe organisationName
    }

    "return updated userAnswers when details are correct is true but no contact info" in {
      val userAnswers = emptyUserAnswers
      val result      = sut.populateUserAnswers(userAnswers, detailsAreCorrect = true)

      result mustBe userAnswers
    }

    "remove contact details when  details are not correct (false)" in {
      val populatedAnswers = emptyUserAnswers
        .withPage(ContactTypePage.primaryContactDetailsPages.contactNamePage, organisationName)
        .withPage(ContactTypePage.primaryContactDetailsPages.contactEmailPage, contactEmail)
        .withPage(ContactTypePage.primaryContactDetailsPages.contactTelephonePage, contactPhone.get)
        .withPage(ContactTypePage.primaryContactDetailsPages.haveTelephonePage, true)

      val updatedAnswers = sut.populateUserAnswers(populatedAnswers, detailsAreCorrect = false)

      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactNamePage) mustBe None
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactEmailPage) mustBe None
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.contactTelephonePage) mustBe None
      updatedAnswers.get(ContactTypePage.primaryContactDetailsPages.haveTelephonePage) mustBe None
    }

  }
}

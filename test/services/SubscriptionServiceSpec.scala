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

import base.SpecBase
import connectors.SubscriptionConnector
import generators.ModelGenerators
import models.subscription.{ContactInformation, IndividualDetails, OrganisationDetails, ResponseDetail}
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase with ModelGenerators {

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
    )
    .build()

  val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]

  "SubscriptionService" - {
    "GetContactDetails" - {
      "must call the subscription connector and return a UserAnswers populated with returned contact details for Organisation" in {
        val responseDetailString: String =
          """
            |{
            |"subscriptionID": "111111111",
            |"tradingName": "",
            |"isGBUser": true,
            |"primaryContact":
            |{
            |"email": "test@test.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "acme"
            |}
            |},
            |"secondaryContact":
            |{
            |"email": "test@test.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "wer"
            |}
            |}
            |}""".stripMargin

        val responseDetail = Json.parse(responseDetailString).as[ResponseDetail]

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.getContactDetails(emptyUserAnswers)

        val ua = result.futureValue.value

        ua.get(ContactNamePage) mustBe Some("acme")
        ua.get(ContactEmailPage) mustBe Some("test@test.com")
        ua.get(ContactPhonePage) mustBe Some("99999")
      }

      "must call the subscription connector and return a UserAnswers populated with returned contact details for Individual" in {
        val responseDetailString: String =
          """
            |{
            |"subscriptionID": "111111111",
            |"tradingName": "",
            |"isGBUser": true,
            |"primaryContact":
            |{
            |"email": "test@test.com",
            |"phone": "99999",
            |"mobile": "",
            |"individual": {
            |"firstName" : "fname",
            |"lastName" : "lname"
            |}
            |}
            |
            |}""".stripMargin

        val responseDetail = Json.parse(responseDetailString).as[ResponseDetail]

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.getContactDetails(emptyUserAnswers)

        val ua = result.futureValue.value

        ua.get(ContactEmailPage) mustBe Some("test@test.com")
        ua.get(ContactPhonePage) mustBe Some("99999")
      }
    }

    "updateContactDetails" - {

      "must return true on updating contactDetails" in {
        val contactDetails = Arbitrary.arbitrary[ResponseDetail].sample.value
        val userAnswers = emptyUserAnswers
          .set(ContactEmailPage, "test@email.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value
          .set(HaveSecondContactPage, true)
          .success
          .value
          .set(SecondContactEmailPage, "test1@email.com")
          .success
          .value
          .set(SecondContactHavePhonePage, true)
          .success
          .value
          .set(SecondContactPhonePage, "+3311211212")
          .success
          .value
        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(true))

        service.updateContactDetails(userAnswers).futureValue mustBe true

      }

      "must return false on failing to update the contactDetails" in {
        val contactDetails = Arbitrary.arbitrary[ResponseDetail].sample.value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(false))

        service.updateContactDetails(emptyUserAnswers).futureValue mustBe false

      }

      "must return false on failing to get response from readSubscription" in {
        val contactDetails = Arbitrary.arbitrary[ResponseDetail].sample.value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(false))

        service.updateContactDetails(emptyUserAnswers).futureValue mustBe false

      }
    }

    "hasResponseDetailDataChanged" - {
      "return false when ReadSubscription data is not changed for individual flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(IndividualDetails("fname", None, "lname"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(ContactEmailPage, "test@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(false)
      }

      "return true when ReadSubscription data is changed for individual flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(IndividualDetails("fname", None, "lname"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(ContactEmailPage, "changetest@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(true)
      }

      "return false when ReadSubscription data is not changed for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(OrganisationDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(ContactNamePage, "orgName")
          .success
          .value
          .set(ContactEmailPage, "test@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(false)
      }

      "return true when ReadSubscription data secondaryContact is None and user updated the secondary contact for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(OrganisationDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(ContactNamePage, "orgName")
          .success
          .value
          .set(ContactEmailPage, "test@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value
          .set(HaveSecondContactPage, true)
          .success
          .value
          .set(SecondContactNamePage, "SecOrgName")
          .success
          .value
          .set(SecondContactEmailPage, "test1@email.com")
          .success
          .value
          .set(SecondContactHavePhonePage, true)
          .success
          .value
          .set(SecondContactPhonePage, "+3311211212")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(true)
      }

      "return true when ReadSubscription data is changed for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = "111111111",
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(OrganisationDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        val userAnswers = emptyUserAnswers
          .set(ContactNamePage, "orgName")
          .success
          .value
          .set(ContactEmailPage, "changetest@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe Some(true)
      }

      "return None when ReadSubscription fails to return the details" in {

        val userAnswers = emptyUserAnswers
          .set(ContactEmailPage, "changetest@test.com")
          .success
          .value
          .set(HaveTelephonePage, true)
          .success
          .value
          .set(ContactPhonePage, "+4411223344")
          .success
          .value

        when(mockSubscriptionConnector.readSubscription()(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(None))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers)
        result.futureValue mustBe None
      }
    }
  }
}

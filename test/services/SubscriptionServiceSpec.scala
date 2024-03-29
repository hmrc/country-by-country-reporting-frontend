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

import base.SpecBase
import connectors.SubscriptionConnector
import generators.ModelGenerators
import models.subscription.{ContactInformation, OrganisationDetails, ResponseDetail}
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

  private val cbcId = "111111111"

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

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.getContactDetails(emptyUserAnswers, cbcId)

        val ua = result.futureValue.value

        ua.get(ContactNamePage) mustBe Some("acme")
        ua.get(ContactEmailPage) mustBe Some("test@test.com")
        ua.get(ContactPhonePage) mustBe Some("99999")
      }

      "must call the subscription connector and return a user answers containing primary contactInformation only" +
        "for the returning user coming for the first time after migration" in {
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
            |"organisationName": "wer"
            |}
            |},
            |"secondaryContact":
            |{
            |"email": "migrated@email.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "MIGRATED"
            |}
            |}
            |}""".stripMargin

          val responseDetail = Json.parse(responseDetailString).as[ResponseDetail]

          when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
            .thenReturn(Future.successful(Some(responseDetail)))

          val result = service.getContactDetails(emptyUserAnswers, cbcId)

          val ua = result.futureValue.value

          val json = Json.parse("""{"primaryClientContactInformation":{"organisation":{"organisationName":"wer"},
            |"email":"test@test.com","phone":"99999","mobile":""}}""".stripMargin)

          ua.data mustBe json
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
        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(true))

        service.updateContactDetails(userAnswers, cbcId).futureValue mustBe true

      }

      "must return false on failing to update the contactDetails" in {
        val contactDetails = Arbitrary.arbitrary[ResponseDetail].sample.value

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(false))

        service.updateContactDetails(emptyUserAnswers, cbcId).futureValue mustBe false

      }

      "must return false on failing to get response from readSubscription" in {
        val contactDetails = Arbitrary.arbitrary[ResponseDetail].sample.value

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(contactDetails)))
        when(mockSubscriptionConnector.updateSubscription(any())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(false))

        service.updateContactDetails(emptyUserAnswers, cbcId).futureValue mustBe false

      }
    }

    "hasResponseDetailDataChanged" - {
      "return false when ReadSubscription data is not changed for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = cbcId,
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

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers, cbcId)
        result.futureValue mustBe Some((false, false))
      }

      "return true when ReadSubscription data secondaryContact is None and user updated the secondary contact for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = cbcId,
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

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers, cbcId)
        result.futureValue mustBe Some((true, false))
      }

      "return true when ReadSubscription data is changed for organisation flow" in {
        val responseDetail = ResponseDetail(
          subscriptionID = cbcId,
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

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers, cbcId)
        result.futureValue mustBe Some((true, false))
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

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(None))

        val result = service.isContactInformationUpdated(userAnswers = userAnswers, cbcId)
        result.futureValue mustBe None
      }
    }
    "doContactDetailsExist" - {
      "must return true if contactDetails exists" in {
        val responseDetail = ResponseDetail(
          subscriptionID = cbcId,
          tradingName = Some("name"),
          isGBUser = true,
          primaryContact = ContactInformation(OrganisationDetails("orgName"), "test@test.com", Some("+4411223344"), Some("4411223344")),
          secondaryContact = None
        )

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.doContactDetailsExist(cbcId)

        result.futureValue.value mustBe true
      }

      "must return false if visiting after migration" in {
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
            |"organisationName": "wer"
            |}
            |},
            |"secondaryContact":
            |{
            |"email": "migrated@email.com",
            |"phone": "99999",
            |"mobile": "",
            |"organisation": {
            |"organisationName": "MIGRATED"
            |}
            |}
            |}""".stripMargin

        val responseDetail = Json.parse(responseDetailString).as[ResponseDetail]

        when(mockSubscriptionConnector.readSubscription(any[String])(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(responseDetail)))

        val result = service.doContactDetailsExist(cbcId)

        result.futureValue.value mustBe false
      }
    }
  }
}

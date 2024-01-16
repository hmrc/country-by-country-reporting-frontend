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

package generators

import models.{AgentClientDetails, ManageYourClients}
import models.agentSubscription.{
  AgentContactInformation,
  AgentDetails,
  AgentRequestCommonForSubscription,
  AgentRequestDetail,
  AgentRequestDetailForUpdate,
  AgentResponseDetail,
  AgentSubscriptionRequest,
  CreateAgentSubscriptionRequest
}
import models.fileDetails.{BusinessRuleErrorCode, FileErrors, FileValidationErrors, RecordError}
import models.subscription._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryAgentClientDetails: Arbitrary[AgentClientDetails] =
    Arbitrary {
      for {
        clientID    <- arbitrary[String]
        tradingName <- arbitrary[String]
      } yield AgentClientDetails(clientID, Some(tradingName))
    }

  implicit lazy val arbitraryWhatToDoNext: Arbitrary[ManageYourClients] =
    Arbitrary {
      Gen.oneOf(ManageYourClients.values.toSeq)
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] = Arbitrary {
    for {
      orgName <- arbitrary[String]
    } yield OrganisationDetails(orgName)
  }

  implicit val arbitraryAgentDetails: Arbitrary[AgentDetails] = Arbitrary {
    for {
      orgName <- arbitrary[String]
    } yield AgentDetails(orgName)
  }

  implicit val arbitraryContactInformation: Arbitrary[ContactInformation] = Arbitrary {
    for {
      contactType <- arbitrary[OrganisationDetails]
      email       <- arbitrary[String]
      phone       <- Gen.option(arbitrary[String])
      mobile      <- Gen.option(arbitrary[String])
    } yield ContactInformation(contactType, email, phone, mobile)
  }

  implicit val arbitraryAgentContactInformation: Arbitrary[AgentContactInformation] = Arbitrary {
    for {
      contactType <- arbitrary[AgentDetails]
      email       <- arbitrary[String]
      phone       <- Gen.option(arbitrary[String])
      mobile      <- Gen.option(arbitrary[String])
    } yield AgentContactInformation(contactType, email, phone, mobile)
  }

  implicit val arbitraryRequestDetail: Arbitrary[RequestDetailForUpdate] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield RequestDetailForUpdate(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryAgentRequestDetailForUpdate: Arbitrary[AgentRequestDetailForUpdate] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[AgentContactInformation]
      secondaryContact <- Gen.option(arbitrary[AgentContactInformation])
    } yield AgentRequestDetailForUpdate(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryBusinessRulesErrorCode: Arbitrary[BusinessRuleErrorCode] = Arbitrary {
    Gen.oneOf[BusinessRuleErrorCode](BusinessRuleErrorCode.values)
  }

  implicit val arbitraryUpdateFileErrors: Arbitrary[FileErrors] = Arbitrary {
    for {
      recordErrorCode <- arbitrary[BusinessRuleErrorCode]
      details         <- Gen.option(arbitrary[String])
    } yield FileErrors(recordErrorCode, details)
  }

  implicit val arbitraryUpdateRecordErrors: Arbitrary[RecordError] = Arbitrary {
    for {
      recordErrorCode <- arbitrary[BusinessRuleErrorCode]
      details         <- Gen.option(arbitrary[String])
      docRefIdRef     <- Gen.option(listWithMaxLength(5, arbitrary[String]))
    } yield RecordError(recordErrorCode, details, docRefIdRef)
  }

  implicit val arbitraryResponseDetail: Arbitrary[ResponseDetail] = Arbitrary {
    for {
      subscriptionID   <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[ContactInformation]
      secondaryContact <- Gen.option(arbitrary[ContactInformation])
    } yield ResponseDetail(subscriptionID, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryAgentResponseDetail: Arbitrary[AgentResponseDetail] = Arbitrary {
    for {
      subscriptionID   <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[AgentContactInformation]
      secondaryContact <- Gen.option(arbitrary[AgentContactInformation])
    } yield AgentResponseDetail(subscriptionID, tradingName, isGBUser, primaryContact, secondaryContact)
  }

  implicit val arbitraryUpdateValidationErrors: Arbitrary[FileValidationErrors] =
    Arbitrary {
      for {
        fileError    <- Gen.option(listWithMaxLength(5, arbitrary[FileErrors]))
        recordErrors <- Gen.option(listWithMaxLength(5, arbitrary[RecordError]))
      } yield FileValidationErrors(fileError, recordErrors)
    }

  implicit val arbitraryAgentRequestDetail: Arbitrary[AgentRequestDetail] = Arbitrary {
    for {
      idType           <- arbitrary[String]
      idNumber         <- arbitrary[String]
      tradingName      <- Gen.option(arbitrary[String])
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[AgentContactInformation]
      secondaryContact <- Gen.option(arbitrary[AgentContactInformation])
    } yield AgentRequestDetail(
      IDType = idType,
      IDNumber = idNumber,
      tradingName = tradingName,
      isGBUser = isGBUser,
      primaryContact = primaryContact,
      secondaryContact = secondaryContact
    )
  }

  implicit val arbitraryAgentRequestCommonForSubscription: Arbitrary[AgentRequestCommonForSubscription] =
    Arbitrary {
      for {
        receiptDate        <- arbitrary[String]
        acknowledgementRef <- arbitrary[String]
      } yield AgentRequestCommonForSubscription(
        regime = "CBC",
        conversationID = None,
        receiptDate = receiptDate,
        acknowledgementReference = acknowledgementRef,
        originatingSystem = "MDTP",
        None
      )
    }

  implicit val arbitraryCreateAgentSubscriptionRequest: Arbitrary[CreateAgentSubscriptionRequest] =
    Arbitrary {
      for {
        requestCommon <- arbitrary[AgentRequestCommonForSubscription]
        requestDetail <- arbitrary[AgentRequestDetail]
      } yield CreateAgentSubscriptionRequest(
        AgentSubscriptionRequest(requestCommon, requestDetail)
      )
    }

  def listWithMaxLength[T](maxSize: Int, gen: Gen[T]): Gen[Seq[T]] =
    for {
      size  <- Gen.choose(1, maxSize)
      items <- Gen.listOfN(size, gen)
    } yield items

}

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

package generators

import models.WhatToDoNext
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
import models.fileDetails.RecordErrorCode.CustomError
import models.fileDetails.{FileErrorCode, FileErrors, RecordError, RecordErrorCode, ValidationErrors}
import models.subscription._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryWhatToDoNext: Arbitrary[WhatToDoNext] =
    Arbitrary {
      Gen.oneOf(WhatToDoNext.values.toSeq)
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

  implicit val arbitraryFileErrorCode: Arbitrary[FileErrorCode] = Arbitrary {
    Gen.oneOf[FileErrorCode](FileErrorCode.values)
  }

  implicit val arbitraryRecordErrorCode: Arbitrary[RecordErrorCode] = Arbitrary {
    Gen.oneOf[RecordErrorCode](RecordErrorCode.values.filterNot(_ == CustomError))
  }

  implicit val arbitraryUpdateFileErrors: Arbitrary[FileErrors] = Arbitrary {
    for {
      fileErrorCode <- arbitrary[FileErrorCode]
      details       <- Gen.option(arbitrary[String])
    } yield FileErrors(fileErrorCode, details)
  }

  implicit val arbitraryUpdateRecordErrors: Arbitrary[RecordError] = Arbitrary {
    for {
      recordErrorCode <- arbitrary[RecordErrorCode]
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

  implicit val arbitraryUpdateValidationErrors: Arbitrary[ValidationErrors] =
    Arbitrary {
      for {
        fileErrors   <- Gen.option(listWithMaxLength(5, arbitrary[FileErrors]))
        recordErrors <- Gen.option(listWithMaxLength(5, arbitrary[RecordError]))
      } yield ValidationErrors(fileErrors, recordErrors)
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

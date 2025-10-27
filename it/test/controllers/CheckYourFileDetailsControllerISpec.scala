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

package controllers

import models.{CBC401, MessageSpecData, TestData, UserAnswers, ValidatedFileData}
import pages._
import utils.ISpecBehaviours

import java.time.LocalDate

class CheckYourFileDetailsControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/check-your-file-details")

  "CheckYourFileDetailsController" must {
    val vfd: ValidatedFileData = ValidatedFileData(
      "filename.xml",
      MessageSpecData("messageRefId", CBC401, TestData, LocalDate.of(2012, 1, 1), LocalDate.of(2016, 1, 1), "testReportingEntity"),
      20L,
      "testChecksum"
    )
    val userAnswersWithContactDetails: UserAnswers = emptyUserAnswers
      .withPage(ContactNamePage, "test")
      .withPage(ContactEmailPage, "test@test.com")
      .withPage(HaveTelephonePage, true)
      .withPage(ContactPhonePage, "6677889922")
      .withPage(HaveSecondContactPage, true)
      .withPage(SecondContactNamePage, "test user")
      .withPage(SecondContactEmailPage, "t2@test.com")
      .withPage(SecondContactHavePhonePage, true)
      .withPage(SecondContactPhonePage, "8889988728")
      .withPage(ValidXMLPage, vfd)

    behave like pageLoads(pageUrl, "checkYourFileDetails.title", userAnswersWithContactDetails)
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}

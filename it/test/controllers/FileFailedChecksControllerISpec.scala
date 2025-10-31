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

import models.{CBC401, ConversationId, MessageSpecData, TestData, ValidatedFileData}
import pages.{ConversationIdPage, ValidXMLPage}
import utils.ISpecBehaviours

import java.time.LocalDate

class FileFailedChecksControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/file-failed-checks")

  "FileFailedChecksController" must {
    val vfd: ValidatedFileData = ValidatedFileData(
      "filename.xml",
      MessageSpecData("messageRefId", CBC401, TestData, LocalDate.of(2012, 1, 1), LocalDate.of(2016, 1, 1), "testReportingEntity"),
      20L,
      "testChecksum"
    )
    val ua = emptyUserAnswers
      .withPage(ValidXMLPage, vfd)
      .withPage(ConversationIdPage, ConversationId("testId"))
    behave like pageLoads(pageUrl, "fileFailedChecks.title", ua)
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}

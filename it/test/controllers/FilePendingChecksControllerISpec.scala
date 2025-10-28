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

import models.{CBC401, MessageSpecData, TestData, ValidatedFileData}
import pages.{ConversationIdPage, ValidXMLPage}
import play.api.http.Status.OK
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBehaviours

import java.time.LocalDate

class FilePendingChecksControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/still-checking-your-file")

  "FilePendingChecksController" must {
    val vfd: ValidatedFileData = ValidatedFileData(
      "filename.xml",
      MessageSpecData("messageRefId", CBC401, TestData, LocalDate.of(2012, 1, 1), LocalDate.of(2016, 1, 1), "testReportingEntity"),
      20L,
      "testChecksum"
    )
    val ua = emptyUserAnswers
      .withPage(ValidXMLPage, vfd)
      .withPage(ConversationIdPage, conversationId)

    "load relative page" in {
      stubAuthorised("cbcId")
      stubGetResponse(fileStatusUrl, OK, pendingFileStatus)

      await(repository.set(ua))

      val response = await(
        buildClient(pageUrl)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      response.body must include(messages("filePendingChecks.title"))

    }

    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}

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

import models.upscan.{Reference, UploadId}
import models.{CBC401, MessageSpecData, TestData, ValidatedFileData}
import pages.{FileReferencePage, URLPage, UploadIDPage, ValidXMLPage}
import utils.ISpecBehaviours

import java.time.LocalDate

class SendYourFileControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/send-your-file")

  private val vfd: ValidatedFileData = ValidatedFileData(
    "filename.xml",
    MessageSpecData("messageRefId", CBC401, TestData, LocalDate.of(2012, 1, 1), LocalDate.of(2016, 1, 1), "testReportingEntity"),
    20L,
    "testChecksum"
  )

  private val ua = userAnswersWithContactDetails
    .withPage(ValidXMLPage, vfd)
    .withPage(URLPage, "http://test.com")
    .withPage(UploadIDPage, UploadId("upload123"))
    .withPage(FileReferencePage, Reference("fileRef123"))

  "SendYourFileController" must {

    behave like pageLoads(pageUrl, "sendYourFile.title", ua)
    behave like pageRedirectsWhenNotAuthorised(pageUrl)

    //behave like pageSubmits(pageUrl, "next page url", ua) //todo
  }

}

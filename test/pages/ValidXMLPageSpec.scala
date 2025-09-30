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

package pages

import base.SpecBase
import models.{CBC401, MessageSpecData, TestData, UserAnswers, ValidatedFileData}
import pages.behaviours.PageBehaviours

class ValidXMLPageSpec extends PageBehaviours with SpecBase {

  "ValidXmlPage" - {
    "must remove invalid xml page when validXmlPage is set" in {
      val userAnswerWithInvalidXml = UserAnswers("some-user-id").set(InvalidXMLPage, "some-xml").success.value

      userAnswerWithInvalidXml.get(InvalidXMLPage).isDefined mustEqual true

      val messageSpec             = MessageSpecData("messageRefId", CBC401, TestData, startDate, endDate, "Reporting Entity")
      val validatedFileData       = ValidatedFileData("filename.xml", messageSpec, 0L, "checksum")
      val userAnswersWithValidXml = userAnswerWithInvalidXml.set(ValidXMLPage, validatedFileData).success.value

      userAnswersWithValidXml.get(InvalidXMLPage).isEmpty mustEqual true
      userAnswersWithValidXml.get(ValidXMLPage).isDefined mustEqual true
    }
  }
}

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

package pages

import base.SpecBase
import models.{CBC401, MessageSpecData, TestData, UserAnswers, ValidatedFileData}
import pages.behaviours.PageBehaviours

class InvalidXMLPageSpec extends PageBehaviours with SpecBase {

  "InvalidXMLPage" - {
    beRetrievable[String](InvalidXMLPage)

    beSettable[String](InvalidXMLPage)

    beRemovable[String](InvalidXMLPage)

    "when invalid xml page is set" - {
      "it should remove valid xml page from user answers" in {
        val messageSpec             = MessageSpecData("messageRefId", CBC401, TestData, startDate, endDate, "Reporting Entity")
        val validateFileData        = ValidatedFileData("filename.xml", messageSpec, 0L, "checksum")
        val validXMLPageUserAnswers = UserAnswers("some-id").set(ValidXMLPage, validateFileData).success.value

        validXMLPageUserAnswers.get(ValidXMLPage).isDefined mustEqual true

        val invalidXMLPageUserAnswers = validXMLPageUserAnswers.set(InvalidXMLPage, "some-xml").success.value
        invalidXMLPageUserAnswers.get(ValidXMLPage).isEmpty mustEqual true
        invalidXMLPageUserAnswers.get(InvalidXMLPage).isDefined mustEqual true
      }

    }
  }
}

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

import models.UserAnswers
import pages.ContactNamePage
import utils.ISpecBehaviours

class SecondContactHavePhoneControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/change-contact/second-contact-have-phone")

  "GET SecondContactHavePhoneController.onPageLoad" must {
    behave like pageLoads(pageUrl, "secondContactHavePhone.title")
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

  "POST SecondContactHavePhoneController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("true"))

    val ua: UserAnswers = emptyUserAnswers
      .withPage(ContactNamePage, "test")

    behave like standardOnSubmit(pageUrl, requestBody)
    behave like pageSubmits(pageUrl, "change-contact/second-contact-phone", ua, requestBody)
  }

}

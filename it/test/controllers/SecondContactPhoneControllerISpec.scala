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

class SecondContactPhoneControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/change-contact/second-contact-phone")

  "GET SecondContactPhoneController.onPageLoad" must {
    behave like pageLoads(pageUrl, "secondContactPhone.title")
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

  "POST SecondContactPhoneController.onSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("66662212"))

    val ua: UserAnswers = emptyUserAnswers
      .withPage(ContactNamePage, "test")

    behave like pageSubmits(pageUrl, "/change-contact/details", ua, requestBody)
    behave like standardOnSubmit(pageUrl, requestBody)
  }

}

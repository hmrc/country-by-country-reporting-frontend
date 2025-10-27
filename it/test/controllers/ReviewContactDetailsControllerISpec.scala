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

import models.subscription.{ContactInformation, OrganisationDetails}
import pages.PrimaryClientContactInformationPage
import play.api.http.Status.OK
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBehaviours

class ReviewContactDetailsControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/change-contact/review-contact-details")

  "ReviewContactDetailsController" must {
// TODO    behave like pageLoads(pageUrl, "reviewContactDetails.title")

//    "load relative page" in {
//      stubAuthorised("testId")
//      stubRegistrationReadSubscription()
//
//      val testContactDetails = ContactInformation(OrganisationDetails("testName"), "test@test.com", None, None)
//
//      await(repository.set(emptyUserAnswers.withPage(PrimaryClientContactInformationPage, testContactDetails)))
//
//      val response = await(
//        buildClient(pageUrl)
//          .addCookies(wsSessionCookie)
//          .get()
//      )
//      response.status mustBe OK
//      response.body must include(messages("reviewContactDetails.title"))
//
//    }

    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}

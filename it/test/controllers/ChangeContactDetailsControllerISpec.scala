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
import pages.{ContactEmailPage, ContactNamePage, ContactPhonePage, HaveSecondContactPage, HaveTelephonePage, JourneyInProgressPage}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBehaviours

class ChangeContactDetailsControllerISpec extends ChangeContactDetailsControllerTestContext {

  private val pageUrl: Option[String] = Some("/change-contact/details")

  "GET ChangeContactDetailsController pageload" in {
    stubAuthorised("testId")
    stubPostResponse(readSubscriptionUrl, OK, responseDetailString)

    await(repository.set(userAnswersWithContactDetails.withPage(JourneyInProgressPage, true)))

    val response = await(
      buildClient(pageUrl)
        .addCookies(wsSessionCookie)
        .get()
    )

    response.status mustBe OK
    response.body must include(messages("CheckContactDetails.title"))
  }

  "GET ChangeContactDetailsController pageRedirectsWhenNotAuthorised" must {
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

  "Post ChangeContactDetailsController standardOnSubmit" must {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("7777"))
    behave like standardOnSubmit(pageUrl, requestBody)
  }

  "Post ContactPhoneController pageSubmits" in {
    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("7777"))

    stubAuthorised("testId")
    stubPostResponse(readSubscriptionUrl, OK, responseDetailString)
    stubPostResponse(updateSubscriptionUrl, OK)

    await(repository.set(ua))

    val response = await(
      buildClient(pageUrl)
        .addCookies(wsSessionCookie)
        .addHttpHeaders("Csrf-Token" -> "nocheck")
        .withFollowRedirects(false)
        .post(requestBody)
    )

    response.status mustBe SEE_OTHER
    response.header("Location").value must include("/change-contact/details-updated")
    verifyPost(authUrl)

  }
}

trait ChangeContactDetailsControllerTestContext extends ISpecBehaviours {
  def answers: UserAnswers = UserAnswers("internalId")

  val ua: UserAnswers = answers
    .withPage(ContactNamePage, "test")
    .withPage(ContactEmailPage, "test@test.com")
    .withPage(HaveTelephonePage, true)
    .withPage(ContactPhonePage, "1234567890")
    .withPage(HaveSecondContactPage, true)

  val readSubscriptionUrl   = s"/country-by-country-reporting/subscription/read-subscription/testId"
  val updateSubscriptionUrl = "/country-by-country-reporting/subscription/update-subscription"

  val responseDetailString: String =
    """
      |{
      |"subscriptionID": "111111111",
      |"tradingName": "",
      |"isGBUser": true,
      |"primaryContact":
      |{
      |"email": "some@email.com",
      |"phone": "7777",
      |"mobile": "77777",
      |"organisation": {
      |"organisationName": "orgName"
      |}
      |},
      |"secondaryContact":
      |{
      |"email": "",
      |"organisation": {
      |"organisationName": ""
      |}
      |}
      |}""".stripMargin

}

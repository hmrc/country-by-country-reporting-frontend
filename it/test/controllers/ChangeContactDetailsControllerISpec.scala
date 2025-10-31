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

///*
// * Copyright 2025 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers
//
//import pages.JourneyInProgressPage
//import play.api.http.Status.{OK, SEE_OTHER}
//import play.api.test.Helpers.{await, defaultAwaitTimeout}
//import utils.ISpecBehaviours
//
//class ChangeContactDetailsControllerISpec extends ISpecBehaviours with TestContext {
//
//  private val pageUrl: Option[String] = Some("/change-contact/details")
//
//  "GET ChangeContactDetailsController pageload" in {
//    stubPostResponse(readSubscriptionUrl, OK, responseDetailString)
//    stubAuthorised("cbcId")
//
//    await(repository.set(userAnswersWithContactDetails.withPage(JourneyInProgressPage, true)))
//
//    val response = await(
//      buildClient(pageUrl)
//        .addCookies(wsSessionCookie)
//        .get()
//    )
//
//    response.status mustBe OK
//    response.body must include(messages("CheckContactDetails.title"))
//  }
//
//  "GET pageRedirectsWhenNotAuthorised" must {
//    behave like pageRedirectsWhenNotAuthorised(pageUrl)
//  }
//
//  "Post ChangeContactDetailsController standardOnSubmit" must {
//    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("7777"))
//
//    behave like standardOnSubmit(pageUrl, requestBody)
//  }
//
//  "Post ContactPhoneController pageSubmits" in {
//    val requestBody: Map[String, Seq[String]] = Map("value" -> Seq("7777"))
//
//    stubPostResponse(readSubscriptionUrl, OK, responseDetailString)
//   //behave like pageSubmits(pageUrl, "change-contact/phone", userAnswersWithContactDetails, requestBody)
//
//    stubAuthorised("testId")
//
//    await(repository.set(emptyUserAnswers))
//
//    val response = await(
//      buildClient(pageUrl)
//        .addCookies(wsSessionCookie)
//        .addHttpHeaders("Csrf-Token" -> "nocheck")
//        .withFollowRedirects(false)
//        .post(requestBody)
//    )
//
//    response.status mustBe SEE_OTHER
//    response.header("Location").value must
//      include("redirectLocation")
//    verifyPost(authUrl)
//
//  }
//
//}
//
//trait TestContext {
//  val readSubscriptionUrl = s"/country-by-country-reporting/subscription/read-subscription/cbcId"
//
//  val responseDetailString: String =
//    """
//      |{
//      |"subscriptionID": "111111111",
//      |"tradingName": "",
//      |"isGBUser": true,
//      |"primaryContact":
//      |{
//      |"email": "",
//      |"phone": "",
//      |"mobile": "",
//      |"organisation": {
//      |"organisationName": "orgName"
//      |}
//      |},
//      |"secondaryContact":
//      |{
//      |"email": "",
//      |"organisation": {
//      |"organisationName": ""
//      |}
//      |}
//      |}""".stripMargin
//
//}

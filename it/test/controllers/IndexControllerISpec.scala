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

import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.*
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import utils.ISpecBehaviours

class IndexControllerISpec extends ISpecBehaviours {
  private val mockAppConf: FrontendAppConfig           = mock[FrontendAppConfig]
  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  "GET / IndexController.onPageLoad" must {
    "return OK when the user is authorised" in {
      stubAuthorised("cbc12345")

      val readSubscriptionUrl = "/country-by-country-reporting/subscription/read-subscription/.*"
      val responseDetailString: String =
        """
          |{
          |"subscriptionID": "111111111",
          |"tradingName": "",
          |"isGBUser": true,
          |"primaryContact":
          |{
          |"email": "",
          |"phone": "",
          |"mobile": "",
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

      stubPostResponse(readSubscriptionUrl, OK, responseDetailString)
      when(mockAppConf.privateBetaEnabled).thenReturn(true)

      await(repository.set(userAnswersWithPrivateBetaPassKey))
      val response = await(
        buildClient()
          .withFollowRedirects(false)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      verifyPost(authUrl)
      response.body must include("Manage your country-by-country report")
    }
    behave like pageRedirectsWhenNotAuthorised(None)
  }

}

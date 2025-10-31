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

import play.api.http.Status.OK
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBehaviours

class FileStatusControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String] = Some("/result-of-automatic-checks")

  "FileStatusController" must {
    "load relative page" in {
      stubAuthorised("cbcId")
      stubGetResponse(allFilesUrls, OK, allFiles)

      await(repository.set(emptyUserAnswers))

      val response = await(
        buildClient(pageUrl)
          .addCookies(wsSessionCookie)
          .get()
      )
      response.status mustBe OK
      response.body must include(messages("fileStatus.title"))

    }

    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

}

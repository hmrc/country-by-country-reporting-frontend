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

package views

import base.SpecBase
import org.jsoup.Jsoup
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{running, stubMessages}
import play.api.test.{FakeRequest, Injecting}
import utils.ViewHelper
import views.html.IndexView

class PhaseBannerSpec extends SpecBase with Injecting with ViewHelper {

  "PhaseBannerSpec" - {

    "layout must render Beta banner when beta-phase feature is enabled" in {
      val testApp = new GuiceApplicationBuilder()
        .configure(conf = Map("features.beta-phase" -> true))
        .build()

      running(testApp) {
        val view = testApp.injector.instanceOf[IndexView] // first page in service

        val html = view(showRecentFiles = false, "testCbcId", isAgent = false)(FakeRequest(), stubMessages())
        val doc  = Jsoup.parse(html.body)

        getPhaseBannerTag(doc) mustEqual "Beta"
      }
    }

    "layout must render Alpha banner when beta-phase feature is disabled" in {
      val view = inject[IndexView]

      val html = view(showRecentFiles = false, "testCbcId", isAgent = false)(FakeRequest(), stubMessages())
      val doc  = Jsoup.parse(html.body)

      getPhaseBannerTag(doc) mustEqual "Alpha"
    }

  }
}

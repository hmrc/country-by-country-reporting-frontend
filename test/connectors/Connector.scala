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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import utils.WireMockHelper

trait Connector extends SpecBase with WireMockHelper with Generators with ScalaCheckPropertyChecks {
  val errorCodes: Gen[Int] = Gen.oneOf(400, 403, 404, 405, 409, 500, 503)

  def stubPostResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    server.stubFor(
      post(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  def stubGetResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    server.stubFor(
      WireMock
        .get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}

/*
 * Copyright 2026 HM Revenue & Customs
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

package config

import base.SpecBase
import play.api.Configuration

class ServiceSpec extends SpecBase {

  private val service = Service(host = "localhost", port = "9000", protocol = "http")

  "Service" - {

    "baseUrl" - {

      "must return the correct base URL" in {
        service.baseUrl mustBe "http://localhost:9000"
      }
    }

    "toString" - {

      "must return the base URL" in {
        service.toString mustBe "http://localhost:9000"
      }
    }

    "convertToString" - {

      "must implicitly convert a Service to its base URL string" in {
        val result: String = service
        result mustBe "http://localhost:9000"
      }
    }

    "configLoader" - {

      "must load a Service from configuration" in {
        val config = Configuration(
          "test-service.host"     -> "localhost",
          "test-service.port"     -> "9000",
          "test-service.protocol" -> "http"
        )

        val result = config.get[Service]("test-service")

        result mustBe Service(host = "localhost", port = "9000", protocol = "http")
      }
    }
  }
}

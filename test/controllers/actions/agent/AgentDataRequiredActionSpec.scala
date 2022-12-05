/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.actions.agent

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.{AgentDataRequest, OptionalAgentDataRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.time.Instant

class AgentDataRequiredActionSpec extends SpecBase {

  class Harness() extends AgentDataRequiredActionImpl {
    def callRefine[A](request: OptionalAgentDataRequest[A]): Future[Either[Result, AgentDataRequest[A]]] = refine(request)
  }

  "Agent Data Required Action" - {

    "when UserAnswers is None" - {

      "must redirect user to the there-is-a-problem page" in {

        val action                   = new Harness()
        val fakeRequest              = FakeRequest()
        val optionalAgentDataRequest = OptionalAgentDataRequest(fakeRequest, "userId", None, "arn")
        val result                   = action.callRefine(optionalAgentDataRequest)

        result.foreach(_ mustBe Left(Redirect(routes.ThereIsAProblemController.onPageLoad())))
      }
    }

    "when UserAnswers is Some" - {

      "must return AgentDataRequest" in {

        val action                   = new Harness()
        val fakeRequest              = FakeRequest()
        val now                      = Instant.now()
        val optionalAgentDataRequest = OptionalAgentDataRequest(fakeRequest, "userId", Some(UserAnswers("userId", lastUpdated = now)), "arn")
        val result                   = action.callRefine(optionalAgentDataRequest)

        result.foreach(_ mustBe Right(AgentDataRequest(fakeRequest, "userId", UserAnswers("userId", lastUpdated = now), "arn")))
      }
    }
  }
}

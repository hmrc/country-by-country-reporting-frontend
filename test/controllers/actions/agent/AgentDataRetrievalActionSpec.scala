/*
 * Copyright 2023 HM Revenue & Customs
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
import models.UserAnswers
import models.requests.OptionalAgentDataRequest
import models.requests.agent.AgentIdentifierRequest
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentDataRetrievalActionSpec extends SpecBase {

  class Harness(sessionRepository: SessionRepository) extends AgentDataRetrievalActionProvider(sessionRepository) {
    def callTransform[A](request: AgentIdentifierRequest[A]): Future[OptionalAgentDataRequest[A]] = transform(request)
  }

  "Agent Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(None)
        val action = new Harness(sessionRepository)

        val fakeRequest = FakeRequest()
        val result      = action.callTransform(AgentIdentifierRequest(fakeRequest, "id", "arn")).futureValue

        result.userAnswers must not be defined
        result.request mustBe fakeRequest
        result.userId mustBe "id"
        result.arn mustBe "arn"
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
        val action = new Harness(sessionRepository)

        val fakeRequest = FakeRequest()
        val result      = action.callTransform(AgentIdentifierRequest(fakeRequest, "id", "arn")).futureValue

        result.userAnswers mustBe defined
        result.request mustBe fakeRequest
        result.userId mustBe "id"
        result.arn mustBe "arn"
      }
    }
  }
}

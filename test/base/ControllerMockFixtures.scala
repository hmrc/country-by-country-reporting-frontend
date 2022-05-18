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

package base

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import repositories.SessionRepository

trait ControllerMockFixtures extends Matchers with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {
  self: SpecBase =>

  def onwardRoute: Call                                  = Call("GET", "/foo")
  final val mockDataRetrievalAction: DataRetrievalAction = mock[DataRetrievalAction]
  final val mockSessionRepository: SessionRepository     = mock[SessionRepository]
  final val mockFrontendAppConfig                        = mock[FrontendAppConfig]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  def messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages                      = messagesApi.preferred(fakeRequest)

  override def beforeEach: Unit = {
    Mockito.reset(
      mockSessionRepository,
      mockDataRetrievalAction
    )
    super.beforeEach()
  }

  protected def retrieveUserAnswersData(userAnswers: UserAnswers): Unit =
    when(mockDataRetrievalAction.apply()).thenReturn(new FakeDataRetrievalAction(Some(userAnswers)))

  protected def retrieveNoData(): Unit =
    when(mockDataRetrievalAction.apply()).thenReturn(new FakeDataRetrievalAction(None))

  override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  // Override to provide custom binding
  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(mockDataRetrievalAction),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
}

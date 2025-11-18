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

package utils

import generators.Generators
import models.UserAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import pages.{ContactEmailPage, ContactNamePage, ContactPhonePage, HaveSecondContactPage, HaveTelephonePage}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.FakeRequest
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

trait ISpecBase extends GuiceOneServerPerSuite with DefaultPlayMongoRepositorySupport[UserAnswers] with ScalaFutures with WireMockHelper with Generators {

  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  implicit val hc: HeaderCarrier    = HeaderCarrier()
  val emptyUserAnswers: UserAnswers = UserAnswers("internalId")

  val userAnswersWithContactDetails: UserAnswers = emptyUserAnswers
    .withPage(ContactNamePage, "test")
    .withPage(ContactEmailPage, "test@test.com")
    .withPage(HaveTelephonePage, true)
    .withPage(ContactPhonePage, "1234567890")
    .withPage(HaveSecondContactPage, false)

  def config: Map[String, String] = Map(
    "microservice.services.auth.host"                         -> WireMockConstants.stubHost,
    "microservice.services.auth.port"                         -> WireMockConstants.stubPort.toString,
    "microservice.services.country-by-country-reporting.host" -> WireMockConstants.stubHost,
    "microservice.services.country-by-country-reporting.port" -> WireMockConstants.stubPort.toString,
    "microservice.services.upscan.port"                       -> WireMockConstants.stubPort.toString,
    "mongodb.uri"                                             -> mongoUri,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"       -> "nocheck"
    // "logger.root"                                             -> "INFO",
    // "logger.controllers"                                      -> "DEBUG"
  )

  def buildClient(path: Option[String] = None): WSRequest = {
    val url = path match {
      case Some(value) => s"http://localhost:$port/send-a-country-by-country-report$value"
      case None        => s"http://localhost:$port/send-a-country-by-country-report"
    }
    app.injector.instanceOf[WSClient].url(url)
  }

  def buildFakeRequest() =
    FakeRequest("GET", s"http://localhost:$port/send-a-country-by-country-report").withSession("authToken" -> "my-token")

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(20, Seconds)))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

  }

}

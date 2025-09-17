package utils

/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package utils

import models.UserAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

trait SpecCommonHelper extends PlaySpec with GuiceOneServerPerSuite
  with DefaultPlayMongoRepositorySupport[UserAnswers]
  with ScalaFutures {

  lazy val repository: SessionRepository = app.injector.instanceOf[SessionRepository]

  def config: Map[String, String] = Map(
    "microservice.services.auth.host" -> WireMockConstants.stubHost,
    "microservice.services.auth.port" -> WireMockConstants.stubPort.toString,
    "mongodb.uri" -> mongoUri
  )

  def buildClient(): WSRequest = {
    app.injector.instanceOf[WSClient].url(s"http://localhost:$port/send-a-country-by-country-report")
  }

  def buildFakeRequest() = {
    FakeRequest("GET", s"http://localhost:$port/send-a-country-by-country-report").withSession("authToken" -> "my-token")
  }

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(20, Seconds)))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

}

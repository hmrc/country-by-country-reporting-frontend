/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.FakeRequest
import uk.gov.hmrc.mongo.test.MongoSupport

trait SpecCommonHelper extends PlaySpec with GuiceOneServerPerSuite with MongoSupport
  with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures {

  lazy val wireMock = new WireMock

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

  override protected def beforeAll(): Unit = {
    wireMock.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    wireMock.resetAll()
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    wireMock.stop()
    super.afterAll()
  }
}

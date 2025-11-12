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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "country-by-country-reporting-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String                    = configuration.get[String]("urls.login")
  val loginContinueUrl: String            = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                  = configuration.get[String]("urls.signOut")
  val registerUrl: String                 = configuration.get[String]("urls.register")
  val guidanceAgentService: String        = configuration.get[String]("urls.guidance.agentService")
  val agentServiceHomeUrl: String         = configuration.get[String]("urls.agentServiceHome")
  val agentServiceNoAssignmentUrl: String = configuration.get[String]("urls.agentServiceNoAssignment")

  val upscanInitiateHost: String        = servicesConfig.baseUrl("upscan")
  val upscanBucketHost: String          = servicesConfig.baseUrl("upscan")
  val upscanProtocol: String            = servicesConfig.getConfString("upscan.protocol", "https")
  val upscanRedirectBase: String        = configuration.get[String]("microservice.services.upscan.redirect-base")
  val upscanCallbackDelayInSeconds: Int = configuration.get[Int]("microservice.services.upscan.callbackDelayInSeconds")
  val upscanMaxFileSize: Int            = configuration.get[Int]("microservice.services.upscan.max-file-size-in-mb")

  val emailEnquiries: String = configuration.get[String]("urls.emailEnquiries")

  val migratedUserName: String  = configuration.get[String]("migrated-user.name")
  val migratedUserEmail: String = configuration.get[String]("migrated-user.email")

  private val feedbackSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val feedbackSurveyUrl: String             = s"$feedbackSurveyBaseUrl/feedback-survey/send-a-country-by-country-report/beta"

  val cbcUrl: String = servicesConfig.baseUrl("country-by-country-reporting")

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val spinnerCounter: Int = configuration.get[Int]("spinner.counter")

  lazy val encryptionEnabled: Boolean = configuration.get[Boolean]("mongodb.encryptionEnabled")
  val maxNormalFileSize: Long         = configuration.get[Long]("max-normal-file-size-bytes")
  val normalFileWaitTime: String      = configuration.get[String]("normal-file-wait-time")
  val largeFileWaitTime: String       = configuration.get[String]("large-file-wait-time")
}

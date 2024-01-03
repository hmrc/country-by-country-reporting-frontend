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

package controllers.actions

import base.SpecBase

import javax.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq => mockEq}
import pages.AgentClientIdPage
import play.api.inject
import play.api.mvc.{BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AgentSubscriptionService
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {

    def onPageLoad() = authAction {
      _ => Results.Ok
    }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    type RetrievalType = Option[String] ~ Enrolments ~ Option[AffinityGroup]

    "the user has a supported affinity group" - {
      "must redirect to the user to the use-agent-services when AGENT and no delegated auth rule" in {
        val authRetrievals: RetrievalType = new ~(new ~(Option("userId"), Enrolments(Set.empty[Enrolment])), Option(AffinityGroup.Agent))

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
          .thenReturn(Future.successful(authRetrievals))

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          redirectLocation(result) mustBe Some(controllers.agent.routes.AgentUseAgentServicesController.onPageLoad.url)
        }
      }

      "must redirect to client not identified when AGENT and delegated auth rule passes but client ID does not match" in {
        val authRetrievals: RetrievalType = new ~(new ~(Some("userId"),
                                                        Enrolments(
                                                          Set(
                                                            Enrolment("HMRC-AS-AGENT").withIdentifier("AgentReferenceNumber", "arn123")
                                                          )
                                                        )
                                                  ),
                                                  Some(AffinityGroup.Agent)
        )

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(
          mockAuthConnector.authorise(mockEq(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50), any[Retrieval[Any]])(any(), any())
        ).thenReturn(Future.successful(authRetrievals), Future.successful(()))

        when(
          mockAuthConnector.authorise(
            mockEq(
              Enrolment("HMRC-CBC-ORG")
                .withIdentifier("cbcId", "NonMatchingId")
                .withDelegatedAuthRule("cbc-auth") or
                Enrolment("HMRC-CBC-NONUK-ORG")
                  .withIdentifier("cbcId", "NonMatchingId")
                  .withDelegatedAuthRule("cbc-auth")
            ),
            any[Retrieval[Any]]
          )(any(), any())
        )
          .thenReturn(Future.failed(new InsufficientEnrolments))

        when(mockSessionRepository.get("userId"))
          .thenReturn(
            Future.successful(
              UserAnswers("userId")
                .set(AgentClientIdPage, "NonMatchingId")
                .fold(_ => None, userAnswers => Some(userAnswers))
            )
          )

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          redirectLocation(result) mustBe Some(controllers.client.routes.ProblemCBCIdController.onPageLoad().url)
        }
      }

      "must allow the user enrolment to continue the journey when AGENT and delegated auth rule passes for HMRC-CBC-ORG enrolment" in {
        val authRetrievals: RetrievalType = new ~(new ~(Some("userId"),
                                                        Enrolments(
                                                          Set(
                                                            Enrolment("HMRC-AS-AGENT").withIdentifier("AgentReferenceNumber", "arn123"),
                                                            Enrolment("HMRC-CBC-ORG").withIdentifier("cbcid", "cbcid1234").withDelegatedAuthRule("cbc-auth")
                                                          )
                                                        )
                                                  ),
                                                  Some(AffinityGroup.Agent)
        )

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[Any]])(any(), any()))
          .thenReturn(Future.successful(authRetrievals), Future.successful(()))

        when(mockSessionRepository.get("userId"))
          .thenReturn(
            Future.successful(
              UserAnswers("userId")
                .set(AgentClientIdPage, "cbcid1234")
                .fold(_ => None, userAnswers => Some(userAnswers))
            )
          )

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe OK
        }
      }
      "must allow the user enrolment to continue the journey when AGENT and delegated auth rule passes for HMRC-CBC-NONUK-ORG enrolment" in {
        val authRetrievals: RetrievalType = new ~(new ~(Some("userId"),
                                                        Enrolments(
                                                          Set(
                                                            Enrolment("HMRC-AS-AGENT").withIdentifier("AgentReferenceNumber", "arn123"),
                                                            Enrolment("HMRC-CBC-NONUK-ORG")
                                                              .withIdentifier("cbcid", "cbcid1234")
                                                              .withDelegatedAuthRule("cbc-auth")
                                                          )
                                                        )
                                                  ),
                                                  Some(AffinityGroup.Agent)
        )

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[Any]])(any(), any()))
          .thenReturn(Future.successful(authRetrievals), Future.successful(()))

        when(mockSessionRepository.get("userId"))
          .thenReturn(
            Future.successful(
              UserAnswers("userId")
                .set(AgentClientIdPage, "cbcid1234")
                .fold(_ => None, userAnswers => Some(userAnswers))
            )
          )

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe OK
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page when INDIVIDUAL" in {

        val authRetrievals = Future.successful(new ~(new ~(Some("id"), Enrolments(Set.empty[Enrolment])), Some(Individual)))

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
          .thenReturn(authRetrievals)

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.IndividualSignInProblemController.onPageLoad.url)
        }
      }

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has an no internal ID" - {

      "must redirect the user to the unauthorised page" in {

        val authRetrievals = Future.successful(new ~(new ~(None, Enrolments(Set.empty[Enrolment])), Some(Organisation)))

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]])(any(), any()))
          .thenReturn(authRetrievals)

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            appConfig,
            mockAgentSubscriptionService,
            bodyParsers,
            mockSessionRepository
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has sufficient enrolments" - {

      "when using HMRC-CBC-ORG enrolment must allow the user to continue" in {
        val authRetrievals: RetrievalType = new ~(new ~(Some("userId"),
                                                        Enrolments(
                                                          Set(
                                                            Enrolment("HMRC-CBC-ORG").withIdentifier("cbcid", "cbcid1234").withDelegatedAuthRule("cbc-auth")
                                                          )
                                                        )
                                                  ),
                                                  Some(AffinityGroup.Organisation)
        )

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[Any]])(any(), any()))
          .thenReturn(Future.successful(authRetrievals), Future.successful(()))

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe OK
        }
      }

      "when using HMRC-CBC-NONUK-ORG enrolment must allow the user to continue" in {
        val authRetrievals: RetrievalType =
          new ~(new ~(Some("userId"),
                      Enrolments(
                        Set(
                          Enrolment("HMRC-CBC-NONUK-ORG").withIdentifier("cbcid", "cbcid1234").withDelegatedAuthRule("cbc-auth")
                        )
                      )
                ),
                Some(AffinityGroup.Organisation)
          )

        val mockAuthConnector = mock[AuthConnector]
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[Any]])(any(), any()))
          .thenReturn(Future.successful(authRetrievals), Future.successful(()))

        running(application) {
          val bodyParsers                  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig                    = application.injector.instanceOf[FrontendAppConfig]
          val mockAgentSubscriptionService = mock[AgentSubscriptionService]

          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, mockAgentSubscriptionService, bodyParsers, mockSessionRepository)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe OK
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}

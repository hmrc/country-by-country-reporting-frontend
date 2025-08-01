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

import com.google.inject.AbstractModule
import controllers.actions._
import controllers.actions.agent._
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[CheckForSubmissionAction]).to(classOf[CheckForSubmissionActionImpl]).asEagerSingleton()
    bind(classOf[ValidationSubmissionDataAction]).to(classOf[OrgValidationSubmissionDataActionImpl]).asEagerSingleton()
    bind(classOf[AgentCheckForSubmissionAction]).to(classOf[AgentCheckForSubmissionActionImpl]).asEagerSingleton()
    bind(classOf[AddJourneyNameAction]).to(classOf[AddJourneyNameActionRefinerImpl]).asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()

    bind(classOf[AgentIdentifierAction]).to(classOf[AuthenticatedAgentIdentifierAction]).asEagerSingleton()
    bind(classOf[AgentDataRetrievalAction]).to(classOf[AgentDataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[AgentDataRequiredAction]).to(classOf[AgentDataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[SignOutAction]).to(classOf[AuthenticatedSignOutAction]).asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    bind(classOf[Encrypter]).toProvider(classOf[CryptoProvider])
    bind(classOf[Decrypter]).toProvider(classOf[CryptoProvider])
  }

}

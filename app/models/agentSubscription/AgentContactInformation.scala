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

package models.agentSubscription

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._

case class AgentDetails(organisationName: String)

object AgentDetails {

  implicit lazy val reads: Reads[AgentDetails] = {
    import play.api.libs.functional.syntax._
    (__ \ "organisation" \ "organisationName").read[String] fmap AgentDetails.apply
  }

  implicit val writes: Writes[AgentDetails] =
    (__ \ "organisation" \ "organisationName").write[String] contramap unlift(AgentDetails.unapply)

}

case class AgentContactInformation(agentDetails: AgentDetails, email: String, phone: Option[String], mobile: Option[String])

object AgentContactInformation {

  implicit lazy val reads: Reads[AgentContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.read[AgentDetails] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(AgentContactInformation.apply _)
  }

  implicit lazy val writes: OWrites[AgentContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.write[AgentDetails] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(AgentContactInformation.unapply))
  }
}

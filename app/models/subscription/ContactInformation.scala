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

package models.subscription

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._

case class OrganisationDetails(organisationName: String)

object OrganisationDetails {

  implicit lazy val reads: Reads[OrganisationDetails] = {
    import play.api.libs.functional.syntax._
    (__ \ "organisation" \ "organisationName").read[String] fmap OrganisationDetails.apply
  }

  implicit val writes: Writes[OrganisationDetails] =
    (__ \ "organisation" \ "organisationName").write[String] contramap unlift(OrganisationDetails.unapply)

  def convertTo(contactName: Option[String]): Option[OrganisationDetails] =
    contactName.map(OrganisationDetails(_))
}

case class ContactInformation(organisationDetails: OrganisationDetails, email: String, phone: Option[String], mobile: Option[String])

object ContactInformation {

  implicit lazy val reads: Reads[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.read[OrganisationDetails] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
    )(ContactInformation.apply _)
  }

  implicit lazy val writes: OWrites[ContactInformation] = {
    import play.api.libs.functional.syntax._
    (
      __.write[OrganisationDetails] and
        (__ \ "email").write[String] and
        (__ \ "phone").writeNullable[String] and
        (__ \ "mobile").writeNullable[String]
    )(unlift(ContactInformation.unapply))
  }
}

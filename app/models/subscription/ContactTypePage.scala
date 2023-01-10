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

package models.subscription

import pages._

sealed trait ContactTypePage {
  def contactNamePage: QuestionPage[String]
  def contactEmailPage: QuestionPage[String]
  def contactTelephonePage: QuestionPage[String]
  def haveTelephonePage: QuestionPage[Boolean]
}

case class PrimaryContactDetailsPages(contactNamePage: QuestionPage[String],
                                      contactEmailPage: QuestionPage[String],
                                      contactTelephonePage: QuestionPage[String],
                                      haveTelephonePage: QuestionPage[Boolean]
) extends ContactTypePage

case class SecondaryContactDetailsPages(contactNamePage: QuestionPage[String],
                                        contactEmailPage: QuestionPage[String],
                                        contactTelephonePage: QuestionPage[String],
                                        haveTelephonePage: QuestionPage[Boolean]
) extends ContactTypePage

object ContactTypePage {

  implicit val primaryContactDetailsPages: PrimaryContactDetailsPages =
    PrimaryContactDetailsPages(ContactNamePage, ContactEmailPage, ContactPhonePage, HaveTelephonePage)

  implicit val secondaryContactDetailsPages: SecondaryContactDetailsPages =
    SecondaryContactDetailsPages(SecondContactNamePage, SecondContactEmailPage, SecondContactPhonePage, SecondContactHavePhonePage)

}

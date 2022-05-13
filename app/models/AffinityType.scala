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

package models

import play.api.mvc.PathBindable
import uk.gov.hmrc.auth.core.AffinityGroup

sealed trait AffinityType

case object Individual extends WithName("individual") with AffinityType
case object Organisation extends WithName("organisation") with AffinityType
case class UnknownAffinityException() extends Exception

object AffinityType {

  def apply(affinityGroup: AffinityGroup): AffinityType =
    affinityGroup match {
      case AffinityGroup.Individual   => Individual
      case AffinityGroup.Organisation => Organisation
      case _                          => throw UnknownAffinityException()
    }

  implicit def regimePathBindable(implicit stringBinder: PathBindable[String]): PathBindable[AffinityType] = new PathBindable[AffinityType] {

    val types = Seq(Individual, Organisation)

    override def bind(key: String, value: String): Either[String, AffinityType] =
      stringBinder.bind(key, value) match {
        case Right(Individual.toString)   => Right(Individual)
        case Right(Organisation.toString) => Right(Organisation)
        case _                            => Left("Unknown Regime")
      }

    override def unbind(key: String, value: AffinityType): String = {
      val regimeValue = types.find(_ == value).map(_.toString).getOrElse(throw UnknownAffinityException())
      stringBinder.unbind(key, regimeValue)
    }
  }

  def toAffinityTypes(string: String): AffinityType =
    string.toLowerCase match {
      case Individual.toString   => Individual
      case Organisation.toString => Organisation
    }

}

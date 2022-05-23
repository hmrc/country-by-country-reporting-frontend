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

package navigation

import models._
import pages._
import play.api.mvc.Call
import controllers.routes

import javax.inject.{Inject, Singleton}

@Singleton
class ContactDetailsNavigator @Inject() () {

  val normalRoutes: (Page) => UserAnswers => Call = {
    case _ => _ => routes.IndexController.onPageLoad
  }

  val checkRouteMap: Page => UserAnswers => Call = {
    case ContactNamePage   => _ => routes.ContactEmailController.onPageLoad()
    case ContactEmailPage  => _ => routes.HaveTelephoneController.onPageLoad()
    case HaveTelephonePage => ua => haveTelephoneRoutes(CheckMode)(ua)
    case ContactPhonePage  => _ => routes.ChangeOrganisationContactDetailsController.onPageLoad()
    case HaveSecondContactPage =>
      ua =>
        yesNoPage(
          ua,
          HaveSecondContactPage,
          routes.SecondContactNameController.onPageLoad(),
          routes.ChangeOrganisationContactDetailsController.onPageLoad()
        )
    case SecondContactNamePage  => _ => routes.SecondContactEmailController.onPageLoad()
    case SecondContactEmailPage => _ => routes.SecondContactHavePhoneController.onPageLoad()
    case SecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          SecondContactHavePhonePage,
          routes.SecondContactPhoneController.onPageLoad(),
          routes.ChangeOrganisationContactDetailsController.onPageLoad()
        )
    case SecondContactPhonePage => _ => routes.ChangeOrganisationContactDetailsController.onPageLoad()
    case _                      => _ => routes.JourneyRecoveryController.onPageLoad() //TODO: Change to routes.ThereIsAProblemController.onPageLoad() when implemented
  }

  private def haveTelephoneRoutes(mode: Mode)(ua: UserAnswers): Call =
    ua.get(HaveTelephonePage) match {
      case Some(hasPhone) if hasPhone =>
        routes.ContactPhoneController.onPageLoad()
      case _ =>
        nextPage(ContactPhonePage, mode, ua)
    }

  def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(routes.JourneyRecoveryController.onPageLoad()) //TODO: Change to routes.ThereIsAProblemController.onPageLoad() when implemented

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}

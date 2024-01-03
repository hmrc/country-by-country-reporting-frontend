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

package navigation

import models._
import pages._
import play.api.mvc.Call
import controllers.routes

import javax.inject.{Inject, Singleton}

@Singleton
class ContactDetailsNavigator @Inject() () {

  val normalRoutes: (Page) => UserAnswers => Call = {
    case ContactNamePage   => _ => routes.ContactEmailController.onPageLoad(NormalMode)
    case ContactEmailPage  => _ => routes.HaveTelephoneController.onPageLoad(NormalMode)
    case HaveTelephonePage => ua => haveTelephoneRoutes(NormalMode)(ua)
    case ContactPhonePage  => _ => routes.HaveSecondContactController.onPageLoad(NormalMode)
    case HaveSecondContactPage =>
      ua =>
        yesNoPage(
          ua,
          HaveSecondContactPage,
          routes.SecondContactNameController.onPageLoad(NormalMode),
          routes.ChangeContactDetailsController.onPageLoad()
        )
    case SecondContactNamePage  => _ => routes.SecondContactEmailController.onPageLoad(NormalMode)
    case SecondContactEmailPage => _ => routes.SecondContactHavePhoneController.onPageLoad(NormalMode)
    case SecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          SecondContactHavePhonePage,
          routes.SecondContactPhoneController.onPageLoad(NormalMode),
          routes.ChangeContactDetailsController.onPageLoad()
        )
    case SecondContactPhonePage => _ => routes.ChangeContactDetailsController.onPageLoad()
    case _                      => _ => routes.ThereIsAProblemController.onPageLoad()
  }

  val checkRouteMap: Page => UserAnswers => Call = {
    case ContactNamePage   => _ => routes.ContactEmailController.onPageLoad(CheckMode)
    case ContactEmailPage  => _ => routes.HaveTelephoneController.onPageLoad(CheckMode)
    case HaveTelephonePage => ua => haveTelephoneRoutes(CheckMode)(ua)
    case ContactPhonePage  => _ => routes.ChangeContactDetailsController.onPageLoad()
    case HaveSecondContactPage =>
      ua =>
        yesNoPage(
          ua,
          HaveSecondContactPage,
          routes.SecondContactNameController.onPageLoad(CheckMode),
          routes.ChangeContactDetailsController.onPageLoad()
        )
    case SecondContactNamePage  => _ => routes.SecondContactEmailController.onPageLoad(CheckMode)
    case SecondContactEmailPage => _ => routes.SecondContactHavePhoneController.onPageLoad(CheckMode)
    case SecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          SecondContactHavePhonePage,
          routes.SecondContactPhoneController.onPageLoad(CheckMode),
          routes.ChangeContactDetailsController.onPageLoad()
        )
    case SecondContactPhonePage => _ => routes.ChangeContactDetailsController.onPageLoad()
    case _                      => _ => routes.ThereIsAProblemController.onPageLoad()
  }

  private def haveTelephoneRoutes(mode: Mode)(ua: UserAnswers): Call =
    ua.get(HaveTelephonePage) match {
      case Some(hasPhone) if hasPhone =>
        routes.ContactPhoneController.onPageLoad(mode)
      case _ =>
        nextPage(ContactPhonePage, mode, ua)
    }

  def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(routes.ThereIsAProblemController.onPageLoad())

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}

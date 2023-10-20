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

package navigation

import controllers.client.routes
import models.subscription.ContactTypePage.primaryContactDetailsPages.haveTelephonePage
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ClientContactDetailsNavigator @Inject() () {

  val normalRoutes: (Page) => UserAnswers => Call = {
    case ReviewClientContactDetailsPage =>
      ua =>
        yesNoPageConditional(
          ua,
          ReviewClientContactDetailsPage,
          ua => ua.get(ContactNamePage).isDefined && ua.get(ContactEmailPage).isDefined,
          routes.ClientHaveSecondContactController.onPageLoad(NormalMode),
          ua => ua.get(ContactNamePage).isEmpty && ua.get(ContactEmailPage).isEmpty,
          routes.ClientFirstContactNameController.onPageLoad(NormalMode)
        )
    case ContactNamePage  => _ => routes.ClientFirstContactEmailController.onPageLoad(NormalMode)
    case ContactEmailPage => _ => routes.ClientFirstContactHavePhoneController.onPageLoad(NormalMode)
    case HaveTelephonePage =>
      ua =>
        yesNoPage(ua,
                  haveTelephonePage,
                  routes.ClientFirstContactPhoneController.onPageLoad(NormalMode),
                  routes.ClientHaveSecondContactController.onPageLoad(NormalMode)
        )

    case ContactPhonePage => _ => routes.ClientHaveSecondContactController.onPageLoad(NormalMode)
    case HaveSecondContactPage =>
      ua =>
        yesNoPage(ua,
                  HaveSecondContactPage,
                  routes.ClientSecondContactNameController.onPageLoad(NormalMode),
                  routes.ChangeClientContactDetailsController.onPageLoad()
        )

    case SecondContactNamePage  => _ => routes.ClientSecondContactEmailController.onPageLoad(NormalMode)
    case SecondContactEmailPage => _ => routes.ClientSecondContactHavePhoneController.onPageLoad(NormalMode)
    case SecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          SecondContactHavePhonePage,
          routes.ClientSecondContactPhoneController.onPageLoad(NormalMode),
          routes.ChangeClientContactDetailsController.onPageLoad()
        )

    case SecondContactPhonePage => _ => routes.ChangeClientContactDetailsController.onPageLoad()
    case _                      => _ => controllers.routes.ThereIsAProblemController.onPageLoad()
  }

  val checkRoutes: (Page) => UserAnswers => Call = {
    case ContactNamePage  => _ => routes.ClientFirstContactEmailController.onPageLoad(CheckMode)
    case ContactEmailPage => _ => routes.ClientFirstContactHavePhoneController.onPageLoad(CheckMode)
    case HaveTelephonePage =>
      ua =>
        yesNoPage(ua,
                  haveTelephonePage,
                  routes.ClientFirstContactPhoneController.onPageLoad(CheckMode),
                  routes.ChangeClientContactDetailsController.onPageLoad()
        )

    case ContactPhonePage => _ => routes.ChangeClientContactDetailsController.onPageLoad()
    case HaveSecondContactPage =>
      ua =>
        yesNoPage(ua,
                  HaveSecondContactPage,
                  routes.ClientSecondContactNameController.onPageLoad(CheckMode),
                  routes.ChangeClientContactDetailsController.onPageLoad()
        )

    case SecondContactNamePage  => _ => routes.ClientSecondContactEmailController.onPageLoad(CheckMode)
    case SecondContactEmailPage => _ => routes.ClientSecondContactHavePhoneController.onPageLoad(CheckMode)
    case SecondContactHavePhonePage =>
      ua =>
        yesNoPage(
          ua,
          SecondContactHavePhonePage,
          routes.ClientSecondContactPhoneController.onPageLoad(CheckMode),
          routes.ChangeClientContactDetailsController.onPageLoad()
        )

    case SecondContactPhonePage => _ => routes.ChangeClientContactDetailsController.onPageLoad()
    case _                      => _ => controllers.routes.ThereIsAProblemController.onPageLoad()
  }

  def yesNoPage(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call =
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.ThereIsAProblemController.onPageLoad())

  private def yesNoPageConditional(ua: UserAnswers,
                                   fromPage: QuestionPage[Boolean],
                                   yesCondition: UserAnswers => Boolean,
                                   yesCall: => Call,
                                   noCondition: UserAnswers => Boolean,
                                   noCall: => Call
  ): Call =
    ua.get(fromPage)
      .map(if (_) {
        if (yesCondition(ua)) {
          yesCall
        } else {
          controllers.routes.ThereIsAProblemController.onPageLoad()
        }
      } else if (noCondition(ua)) {
        noCall
      } else {
        controllers.routes.ThereIsAProblemController.onPageLoad()
      })
      .getOrElse(controllers.routes.ThereIsAProblemController.onPageLoad())

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode => normalRoutes(page)(userAnswers)
    case CheckMode  => checkRoutes(page)(userAnswers)
  }
}

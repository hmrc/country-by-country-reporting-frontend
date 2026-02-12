/*
 * Copyright 2026 HM Revenue & Customs
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

package filters

import config.FrontendAppConfig
import org.apache.pekko.stream.Materializer

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.mvc.*
import play.api.Logging
import play.api.mvc.Results.Redirect
import utils.IpAddressHelper
import utils.IpAddressHelper.*

class InterruptPageFilter @Inject() (appConfig: FrontendAppConfig)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter with Logging {

  override def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    logger.info(s"InterruptPageFilter applied to request: ${requestHeader.uri}")

    if (shouldRedirect(requestHeader)) {
      Future.successful(
        Redirect(controllers.routes.InterruptPageController.onPageLoad())
      )
    } else {
      nextFilter(requestHeader)
    }
  }

  private def shouldRedirect(request: RequestHeader): Boolean =
    appConfig.interruptPageEnabled
      && !appConfig.excludedInterruptsPaths.exists(request.path.contains(_))
      && appConfig.allowedIpList.contains(getClientIp(request))
}

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

package controllers

import connectors.UpscanConnector
import controllers.actions._
import forms.UploadFileFormProvider
import models.requests.DataRequest
import models.upscan.{Failed, Quarantined, URL, UploadRejected, UploadedSuccessfully}
import navigation.Navigator
import pages.UploadIDPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UploadFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UploadFileController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UploadFileFormProvider,
  upscanConnector: UpscanConnector,
  val controllerComponents: MessagesControllerComponents,
  view: UploadFileView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      toResponse(form)
  }

  private def toResponse(preparedForm: Form[String])(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    (for {
      upscanInitiateResponse <- upscanConnector.getUpscanFormData
      uploadId               <- upscanConnector.requestUpload(upscanInitiateResponse.fileReference)
      updatedAnswers         <- Future.fromTry(request.userAnswers.set(UploadIDPage, uploadId))
      _                      <- sessionRepository.set(updatedAnswers)
    } yield Ok(view(preparedForm, upscanInitiateResponse)))
      .recover {
        case e: Exception =>
          logger.warn(s"UploadFileController: An exception occurred when contacting Upscan: $e")
          Redirect(routes.ThereIsAProblemController.onPageLoad())
      }

  def showError(errorCode: String, errorMessage: String, errorRequestId: String): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      errorCode match {
        case "EntityTooLarge" =>
          Future.successful(Redirect(routes.FileProblemTooLargeController.onPageLoad()))
        case "InvalidArgument" | "OctetStream" =>
          val formWithErrors: Form[String] = form.withError("file-upload", "uploadFile.error.file.empty")
          toResponse(formWithErrors)
        case _ =>
          logger.warn(s"Upscan error $errorCode: $errorMessage, requestId is $errorRequestId")
          Future.successful(Redirect(routes.ThereIsAProblemController.onPageLoad()))
      }
  }

  def getStatus: Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      println("\n\n\n\nBeing called\n\n\n\n\n")
      request.userAnswers.get(UploadIDPage) match {
        case Some(uploadId) =>
          upscanConnector.getUploadStatus(uploadId) flatMap {
            case Some(_: UploadedSuccessfully) =>
              //ToDo redirect to validation controller when ready
              Future.successful(Ok(Json.toJson(URL(routes.IndexController.onPageLoad.url))))
            case Some(r: UploadRejected) =>
              if (r.details.message.contains("octet-stream")) {
                logger.warn(s"Show errorForm on rejection $r")
                val errorReason = r.details.failureReason
                Future.successful(Ok(Json.toJson(URL(routes.UploadFileController.showError("OctetStream", errorReason, "").url))))
              } else {
                logger.warn(s"Upload rejected. Error details: ${r.details}")
                Future.successful(Ok(Json.toJson(URL(routes.FileProblemTooLargeController.onPageLoad().url))))
              }
            case Some(Quarantined) =>
              Future.successful(Ok(Json.toJson(URL(routes.FileProblemVirusController.onPageLoad().url))))
            case Some(Failed) =>
              Future.successful(Ok(Json.toJson(URL(routes.ThereIsAProblemController.onPageLoad().url))))
            case Some(_) =>
              Future.successful(Continue)
            case None =>
              Future.successful(Ok(Json.toJson(URL(routes.ThereIsAProblemController.onPageLoad().url))))
          }
        case None =>
          Future.successful(Ok(Json.toJson(URL(routes.ThereIsAProblemController.onPageLoad().url))))
      }
  }
}

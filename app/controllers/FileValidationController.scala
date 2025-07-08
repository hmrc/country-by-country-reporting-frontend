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

package controllers

import connectors.{UpscanConnector, ValidationConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.upscan.{ExtractedFileStatus, FileValidateRequest, UploadId, UploadSessionDetails, UploadedSuccessfully}
import models.{InvalidXmlError, NormalMode, ValidatedFileData, ValidationErrors}
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CBCConstants.{invalidArgumentErrorMessage, invalidFileNameLength, maxFileNameLength}
import views.html.ThereIsAProblemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileValidationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  upscanConnector: UpscanConnector,
  requireData: DataRequiredAction,
  validationConnector: ValidationConnector,
  navigator: Navigator,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(UploadIDPage)
        .fold {
          logger.error("Cannot find upload Id from user answers")
          Future.successful(InternalServerError(errorView()))
        } {
          uploadId =>
            {
              upscanConnector.getUploadDetails(uploadId) map {
                uploadSessions =>
                  getDownloadUrl(uploadSessions).fold {
                    logger.error(s"Failed to upload file with upload Id: [${uploadId.value}]")
                    Future.successful(InternalServerError(errorView()))
                  } {
                    downloadDetails =>
                      val downloadUrl = downloadDetails.downloadUrl
                      val fileName    = downloadDetails.name
                      if (isFileNameInvalid(fileName)) {
                        navigateToErrorPage(uploadId, fileName)
                      } else {
                        validationConnector.sendForValidation(FileValidateRequest(downloadUrl, uploadId.value, request.subscriptionId)) flatMap {
                          case Right(messageSpecData) =>
                            val validatedFileData = ValidatedFileData(fileName, messageSpecData, downloadDetails.size, downloadDetails.checksum)
                            for {
                              updatedAnswers        <- Future.fromTry(request.userAnswers.set(ValidXMLPage, validatedFileData))
                              updatedAnswersWithURL <- Future.fromTry(updatedAnswers.set(URLPage, downloadUrl))
                              _                     <- sessionRepository.set(updatedAnswersWithURL)
                            } yield Redirect(navigator.nextPage(ValidXMLPage, NormalMode, updatedAnswers))

                          case Left(ValidationErrors(errors, _)) =>
                            for {
                              updatedAnswers           <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, fileName))
                              updatedAnswersWithErrors <- Future.fromTry(updatedAnswers.set(GenericErrorPage, errors))
                              _                        <- sessionRepository.set(updatedAnswersWithErrors)
                            } yield Redirect(navigator.nextPage(InvalidXMLPage, NormalMode, updatedAnswers))

                          case Left(InvalidXmlError(_)) =>
                            for {
                              updatedAnswers <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, fileName))
                              _              <- sessionRepository.set(updatedAnswers)
                            } yield Redirect(routes.FileErrorController.onPageLoad())

                          case _ =>
                            Future.successful(InternalServerError(errorView()))
                        }
                      }
                  }
              }
            }.flatten
        }
  }

  private def navigateToErrorPage(uploadId: UploadId, fileName: String) = {
    logger.error(s"file name length is more than allowed limit : $fileName")
    Future.successful(
      Redirect(
        routes.UploadFileController
          .showError(invalidArgumentErrorMessage, invalidFileNameLength, uploadId.value)
          .url
      )
    )
  }

  private def isFileNameInvalid(fileName: String) = fileName.replace(".xml", "").length > maxFileNameLength

  private def getDownloadUrl(uploadSessions: Option[UploadSessionDetails]): Option[ExtractedFileStatus] =
    uploadSessions match {
      case Some(uploadDetails) =>
        uploadDetails.status match {
          case UploadedSuccessfully(name, downloadUrl, size, checksum) =>
            Option(ExtractedFileStatus(name, downloadUrl, size, checksum))
          case _ => None
        }
      case _ => None
    }
}

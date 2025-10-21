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
import models.requests.DataRequest
import models.upscan._
import models.{InvalidXmlError, NormalMode, UserAnswers, ValidatedFileData, ValidationErrors}
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CBCConstants._
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
      extractIds(request.userAnswers) match {
        case Some((uploadId, fileReference)) =>
          {
            upscanConnector.getUploadDetails(uploadId) map {
              uploadSessions =>
                getDownloadUrl(uploadSessions).fold {
                  logger.error(s"Failed to upload file with upload Id: [${uploadId.value}]")
                  Future.successful(InternalServerError(errorView()))
                } {
                  downloadDetails: ExtractedFileStatus =>
                    val trimmedFileName = downloadDetails.name.stripSuffix(".xml")
                    (isFileNameLengthInvalid(trimmedFileName), isDisallowedCharactersPresent(trimmedFileName)) match {
                      case (true, _) => navigateToErrorPage(uploadId, invalidFileNameLength)
                      case (_, true) => navigateToErrorPage(uploadId, disallowedCharacters)
                      case _         => handleFileValidation(downloadDetails, uploadId, fileReference)
                    }
                }
            }
          }.flatten

        case None =>
          logger.error("Missing Upload ID or File Reference from user answers")
          Future.successful(InternalServerError(errorView()))
      }
  }

  private def handleFileValidation(
    downloadDetails: ExtractedFileStatus,
    uploadId: UploadId,
    fileReference: Reference
  )(implicit request: DataRequest[_]) = {
    val downloadUrl = downloadDetails.downloadUrl
    validationConnector.sendForValidation(
      FileValidateRequest(downloadUrl, uploadId.value, request.subscriptionId, fileReference.value)
    ) flatMap {
      case Right(messageSpecData) =>
        val validatedFileData = ValidatedFileData(downloadDetails.name, messageSpecData, downloadDetails.size, downloadDetails.checksum)
        for {
          updatedAnswers        <- Future.fromTry(request.userAnswers.set(ValidXMLPage, validatedFileData))
          updatedAnswersWithURL <- Future.fromTry(updatedAnswers.set(URLPage, downloadUrl))
          _                     <- sessionRepository.set(updatedAnswersWithURL)
        } yield Redirect(navigator.nextPage(ValidXMLPage, NormalMode, updatedAnswers))

      case Left(ValidationErrors(errors, _)) =>
        for {
          updatedAnswers           <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, downloadDetails.name))
          updatedAnswersWithErrors <- Future.fromTry(updatedAnswers.set(GenericErrorPage, errors))
          _                        <- sessionRepository.set(updatedAnswersWithErrors)
        } yield Redirect(navigator.nextPage(InvalidXMLPage, NormalMode, updatedAnswers))

      case Left(InvalidXmlError(_)) =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(InvalidXMLPage, downloadDetails.name))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(routes.FileErrorController.onPageLoad())

      case _ =>
        Future.successful(InternalServerError(errorView()))
    }
  }

  private def extractIds(answers: UserAnswers): Option[(UploadId, Reference)] =
    for {
      uploadId      <- answers.get(UploadIDPage)
      fileReference <- answers.get(FileReferencePage)
    } yield (uploadId, fileReference)

  private def navigateToErrorPage(uploadId: UploadId, errorType: String) =
    Future.successful(
      Redirect(
        routes.UploadFileController
          .showError(invalidArgumentErrorMessage, errorType, uploadId.value)
          .url
      )
    )

  private def isFileNameLengthInvalid(fileName: String) = fileName.length > maxFileNameLength

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

  private def isDisallowedCharactersPresent(trimmedFileName: String) = disallowedCharactersList.findFirstIn(trimmedFileName).isDefined
}

/*
 * Copyright 2025 HM Revenue & Customs
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

import models.upscan.{PreparedUpload, Reference, UploadForm}
import play.api.http.Status.OK
import play.api.libs.json.{Format, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.ISpecBehaviours

class UploadFileControllerISpec extends ISpecBehaviours {

  private val pageUrl: Option[String]    = Some("/upload-file")
  private val upscanInitiatePath: String = "/upscan/v2/initiate"
  private val upscanUploadPath: String   = "/country-by-country-reporting/upscan/upload"

  "UploadFileController pageRedirectsWhenNotAuthorised" must {
    behave like pageRedirectsWhenNotAuthorised(pageUrl)
  }

  "UploadFileController pageLoads" in {
    val body       = PreparedUpload(Reference("Reference"), UploadForm("downloadUrl", Map("formKey" -> "formValue")))
    val uploadBody = UpscanUploadRequest(UploadId("12345"), "Reference")

    stubAuthorised("cbcId")
    stubPostResponse(upscanInitiatePath, OK, Json.toJson(body).toString())
    stubPostResponse(upscanUploadPath, OK, Json.toJson(uploadBody).toString())

    await(repository.set(userAnswersWithContactDetails))

    val response = await(
      buildClient(pageUrl)
        .addCookies(wsSessionCookie)
        .get()
    )
    response.status mustBe OK
    response.body must include(messages("uploadFile.title"))
  }
}

case class UploadId(value: String)

case class UpscanUploadRequest(uploadId: UploadId, fileReference: String)

object UpscanUploadRequest {
  implicit val uploadIdFormFormat: Format[UploadId] = Json.format[UploadId]
  implicit val format: Format[UpscanUploadRequest]  = Json.format[UpscanUploadRequest]
}

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

package utils

import models.ConversationId

trait FileUploadStubs {

  val cbcId = "cbcId"

  val conversationId: ConversationId = ConversationId("conversationId3")

  val allFilesUrls  = s"/country-by-country-reporting/files/details/$cbcId"
  val fileUrl       = s"/country-by-country-reporting/files/${conversationId.value}/details"
  val fileStatusUrl = s"/country-by-country-reporting/files/${conversationId.value}/status"

  val allFiles: String = """
                                   |[
                                   |  {
                                   |    "name": "test1.xml",
                                   |    "messageRefId": "messageRefId1",
                                   |    "reportingEntityName": "Reporting Entity",
                                   |    "reportType": "NEW_INFORMATION",
                                   |    "submitted": "2022-02-10T15:35:37.636",
                                   |    "lastUpdated": "2022-02-10T15:35:37.636",
                                   |    "status":{"Pending":{}},
                                   |    "conversationId": "conversationId1"
                                   |  },
                                   |  {
                                   |    "name": "test2.xml",
                                   |    "messageRefId": "messageRefId2",
                                   |    "reportingEntityName": "Reporting Entity",
                                   |    "reportType": "NEW_INFORMATION",
                                   |    "submitted": "2022-02-10T15:35:37.636",
                                   |    "lastUpdated": "2022-02-10T15:45:37.636",
                                   |    "status": {
                                   |    "Rejected":{
                                   |      "error":{"fileError":[{"code":"50009","details":"Duplicate message ref ID"}],"recordError":[{"code":"80010","details":"A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both","docRefIDInError":["asjdhjjhjssjhdjshdAJGSJJS"]}]}
                                   |      }
                                   |    },
                                   |    "conversationId": "conversationId2"
                                   |  }
                                   |]""".stripMargin

  val file: String = """
                               |  {
                               |    "name": "test3.xml",
                               |    "messageRefId": "messageRefId3",
                               |    "reportingEntityName": "Reporting Entity",
                               |    "reportType": "NEW_INFORMATION",
                               |    "submitted": "2022-02-10T15:35:37.636",
                               |    "lastUpdated": "2022-02-10T15:45:37.636",
                               |    "status": {"Accepted":{}},
                               |    "conversationId": "conversationId3"
                               |  }""".stripMargin

  val pendingFile = """
                               |  {
                               |    "name": "test5.xml",
                               |    "messageRefId": "messageRefId5",
                               |    "reportingEntityName": "Reporting Entity",
                               |    "reportType": "NEW_INFORMATION",
                               |    "submitted": "2022-02-10T15:35:37.636",
                               |    "lastUpdated": "2022-02-10T15:45:37.636",
                               |    "status": {"Pending":{}},
                               |    "conversationId": "conversationId5"
                               |  }""".stripMargin

  val rejectedFile: String = """
                               |  {
                               |    "name": "test4.xml",
                               |    "messageRefId": "messageRefId4",
                               |    "reportingEntityName": "Reporting Entity",
                               |    "reportType": "NEW_INFORMATION",
                               |    "submitted": "2022-02-10T15:35:37.636",
                               |    "lastUpdated": "2022-02-10T15:45:37.636",
                               |    "status": {
                               |    "Rejected":{
                               |      "error":{"fileError":[{"code":"50009","details":"Duplicate message ref ID"}],"recordError":[{"code":"80010","details":"A message can contain either new records (OECD1) or corrections/deletions (OECD2 and OECD3), but cannot contain a mixture of both","docRefIDInError":["asjdhjjhjssjhdjshdAJGSJJS"]}]}
                               |      }
                               |    },
                               |    "conversationId": "conversationId4"
                               |  }""".stripMargin

  val acceptedFileStatus: String = """{"Accepted":{}}""".stripMargin
  val pendingFileStatus: String  = """{"Pending":{}}""".stripMargin

}

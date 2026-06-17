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

package forms.mappings

import base.SpecBase
import models.Enumerable
import play.api.data.FormError

class FormattersSpec extends SpecBase with Formatters {

  "stringFormatter" - {
    val formatter = stringFormatter("error.required")

    "must bind a valid string" in {
      formatter.bind("key", Map("key" -> "foo")) mustBe Right("foo")
    }

    "must return an error when the key is missing" in {
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is empty" in {
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is whitespace only" in {
      formatter.bind("key", Map("key" -> "   ")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must unbind a value" in {
      formatter.unbind("key", "foo") mustBe Map("key" -> "foo")
    }
  }

  "booleanFormatter" - {
    val formatter = booleanFormatter("error.required", "error.invalid")

    "must bind true" in {
      formatter.bind("key", Map("key" -> "true")) mustBe Right(true)
    }

    "must bind false" in {
      formatter.bind("key", Map("key" -> "false")) mustBe Right(false)
    }

    "must return an error when the key is missing" in {
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is empty" in {
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is not a boolean" in {
      formatter.bind("key", Map("key" -> "maybe")) mustBe Left(Seq(FormError("key", "error.invalid")))
    }

    "must unbind a boolean value" in {
      formatter.unbind("key", true) mustBe Map("key" -> "true")
      formatter.unbind("key", false) mustBe Map("key" -> "false")
    }
  }

  "intFormatter" - {
    val formatter = intFormatter("error.required", "error.wholeNumber", "error.nonNumeric")

    "must bind a valid integer" in {
      formatter.bind("key", Map("key" -> "42")) mustBe Right(42)
    }

    "must bind an integer with commas" in {
      formatter.bind("key", Map("key" -> "1,000")) mustBe Right(1000)
    }

    "must return an error when the key is missing" in {
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is empty" in {
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return a wholeNumber error when the value is a decimal" in {
      formatter.bind("key", Map("key" -> "1.5")) mustBe Left(Seq(FormError("key", "error.wholeNumber")))
    }

    "must return a nonNumeric error when the value is not a number" in {
      formatter.bind("key", Map("key" -> "abc")) mustBe Left(Seq(FormError("key", "error.nonNumeric")))
    }

    "must unbind an integer value" in {
      formatter.unbind("key", 42) mustBe Map("key" -> "42")
    }
  }

  "enumerableFormatter" - {
    sealed trait Pies
    case object Pork extends Pies
    case object Macaroni extends Pies

    implicit val enumerable: Enumerable[Pies] = Enumerable(
      "Pork" -> Pork
    )

    val formatter = enumerableFormatter[Pies]("error.required", "error.invalid")

    "must bind a valid enum value" in {
      formatter.bind("key", Map("key" -> "Pork")) mustBe Right(Pork)
    }

    "must return an error when the key is missing" in {
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is empty" in {
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an invalid error when the value does not match any enum" in {
      formatter.bind("key", Map("key" -> "Macaroni")) mustBe Left(Seq(FormError("key", "error.invalid")))
    }

    "must unbind an enum value" in {
      formatter.unbind("key", Pork) mustBe Map("key" -> "Pork")
    }
  }

  "stringTrimFormatter" - {
    "must bind a valid string and trim it" in {
      val formatter = stringTrimFormatter("error.required")
      formatter.bind("key", Map("key" -> "  foo  ")) mustBe Right("foo")
    }

    "must replace non-breaking spaces with regular spaces" in {
      val formatter = stringTrimFormatter("error.required")
      formatter.bind("key", Map("key" -> "foo\u00A0bar")) mustBe Right("foo bar")
    }

    "must return an error without args when the key is missing and no msgArg" in {
      val formatter = stringTrimFormatter("error.required")
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error with args when the key is missing and msgArg is set" in {
      val formatter = stringTrimFormatter("error.required", "someArg")
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required", Seq("someArg"))))
    }

    "must return an error without args when the value is empty and no msgArg" in {
      val formatter = stringTrimFormatter("error.required")
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error with args when the value is empty and msgArg is set" in {
      val formatter = stringTrimFormatter("error.required", "someArg")
      formatter.bind("key", Map("key" -> "   ")) mustBe Left(Seq(FormError("key", "error.required", Seq("someArg"))))
    }

    "must unbind a value" in {
      val formatter = stringTrimFormatter("error.required")
      formatter.unbind("key", "foo") mustBe Map("key" -> "foo")
    }
  }

  "validatedTextFormatter" - {
    val regex = """^[a-zA-Z ]+$"""
    val formatter = validatedTextFormatter(
      requiredKey = "error.required",
      invalidKey = "error.invalid",
      lengthKey = "error.length",
      regex = regex,
      maxLength = 10,
      minLength = 2
    )

    "must bind a valid string" in {
      formatter.bind("key", Map("key" -> "testName")) mustBe Right("testName")
    }

    "must return an error when the key is missing" in {
      formatter.bind("key", Map.empty) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an error when the value is empty" in {
      formatter.bind("key", Map("key" -> "")) mustBe Left(Seq(FormError("key", "error.required")))
    }

    "must return an invalid error when the value does not match the regex" in {
      formatter.bind("key", Map("key" -> "Al1ce!")) mustBe Left(Seq(FormError("key", "error.invalid")))
    }

    "must return a length error when the value exceeds maxLength" in {
      formatter.bind("key", Map("key" -> "a" * 11)) mustBe Left(Seq(FormError("key", "error.length")))
    }

    "must return a length error when the value is below minLength" in {
      formatter.bind("key", Map("key" -> "a")) mustBe Left(Seq(FormError("key", "error.length")))
    }

    "must unbind a value" in {
      formatter.unbind("key", "testName") mustBe Map("key" -> "testName")
    }
  }
}

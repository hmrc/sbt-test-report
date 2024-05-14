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

package uk.gov.hmrc.testreport.model

sealed trait ExclusionRule {
  val maybePathRegex: Option[String]
  val maybeHtmlRegex: Option[String]
  val reason: String

  // TODO apply these once instead of per violation
  private def ensureRegexPattern(str: String): String = {
    val withLeading  = if (!str.startsWith(".*")) s".*$str" else str
    val withTrailing = if (!withLeading.endsWith(".*")) s"$withLeading.*" else withLeading
    withTrailing
  }

  def appliesTo(violation: AxeViolation): Boolean =
    maybePathRegex.exists(pathRegex => violation.url.matches(ensureRegexPattern(pathRegex))) ||
      maybeHtmlRegex.exists(htmlRegex => violation.html.matches(ensureRegexPattern(htmlRegex)))

  def withErrorsHighlighted: String = {
    def arrow(str: String): String =
      if (str.isEmpty) "\t\t<------" else ""

    val path = maybePathRegex.getOrElse("")
    "{" +
      s"""\n "path"  : "$path", ${arrow(path)}""" +
      s"""\n "reason": "$reason" ${arrow(reason)}""" +
      "\n}"
  }

}

case class GlobalExclusionRule(maybeHtmlRegex: Option[String], maybePathRegex: Option[String], reason: String)
    extends ExclusionRule

case class ServiceExclusionRule(maybePathRegex: Option[String], reason: String) extends ExclusionRule {
  val maybeHtmlRegex: Option[String] = None
}

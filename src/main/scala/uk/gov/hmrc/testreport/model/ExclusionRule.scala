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
  val ruleScope: String
  val maybePathRegex: Option[RegexPattern]
  val maybeHtmlRegex: Option[RegexPattern]
  val reason: String

  def appliesTo(location: Location): Boolean =
    maybePathRegex.exists(_.matches(location.url)) ||
      maybeHtmlRegex.exists(_.matches(location.html))

  def withErrorsHighlighted: String = {
    def arrow(str: String): String =
      if (str.isEmpty) "\t\t<------" else ""

    val path = maybePathRegex.map(_.raw).getOrElse("")
    "{" +
      s"""\n "path"  : "$path", ${arrow(path)}""" +
      s"""\n "reason": "$reason" ${arrow(reason)}""" +
      "\n}"
  }

}

case class PlatformExclusionRule(
  maybeHtmlRegex: Option[RegexPattern],
  maybePathRegex: Option[RegexPattern],
  reason: String
) extends ExclusionRule {
  final val ruleScope: String = "Platform"
}

case class ServiceExclusionRule(maybePathRegex: Option[RegexPattern], reason: String) extends ExclusionRule {
  final val ruleScope: String              = "Service"
  val maybeHtmlRegex: Option[RegexPattern] = None
}

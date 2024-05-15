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

class RegexPattern(val pattern: String) extends AnyVal {
  def matches(candidate: String): Boolean = candidate.matches(pattern)
  def raw: String                         = pattern.substring(2, pattern.length - 2)
}

object RegexPattern {
  def apply(rawPattern: String): RegexPattern = {
    val withLeading  = if (rawPattern.startsWith(".*")) rawPattern else s".*$rawPattern"
    val withTrailing = if (withLeading.endsWith(".*")) withLeading else s"$withLeading.*"
    new RegexPattern(withTrailing)
  }
}

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

package uk.gov.hmrc.testreport.plugin

import uk.gov.hmrc.testreport.model.{AxeViolation, ExclusionRule}

object ExclusionRuleReader {

  type ExcludedViolations = List[AxeViolation]
  type IncludedViolations = List[AxeViolation]

  /**
   * Enhances a given path string to match anywhere within another string.
   *
   * The `matches` method in Scala requires the entire string to match the regex pattern.
   * This method ensures the path can match any part of a string by:
   * 1. Prepending `".*"` to the start if not already present, allowing for any characters (or none) before the path.
   * 2. Appending `".*"` to the end if not already present, allowing for any characters (or none) after the path.
   *
   * This modification is necessary because `matches` does not perform partial matches. For simpler substring checks
   * without regex, consider using `contains`.
   *
   * @param str The path string to be enhanced for regex matching.
   * @return A regex pattern string that matches the given path anywhere within another string.
   */
  private def ensureRegexPattern(str: String): String = {
    val withLeading  = if (!str.startsWith(".*")) s".*$str" else str
    val withTrailing = if (!withLeading.endsWith(".*")) s"$withLeading.*" else withLeading
    withTrailing
  }

  def partitionViolations(
    rawAxeViolations: List[AxeViolation],
    a11yExclusions: List[ExclusionRule]
  ): (ExcludedViolations, IncludedViolations) =
    rawAxeViolations
      .foldLeft((List.empty[AxeViolation], List.empty[AxeViolation])) { case ((excluded, included), violation) =>
        a11yExclusions.find(rule => violation.url.matches(ensureRegexPattern(rule.path))) match {
          case Some(rule) => (excluded :+ violation.copy(exclusionRule = Some(rule)), included)
          case None       => (excluded, included :+ violation)
        }
      }
}

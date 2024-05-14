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

  def partitionViolations(
    rawAxeViolations: List[AxeViolation],
    exclusionRules: List[ExclusionRule]
  ): (ExcludedViolations, IncludedViolations) =
    rawAxeViolations
      .foldLeft((List.empty[AxeViolation], List.empty[AxeViolation])) { case ((excluded, included), violation) =>
        exclusionRules.find(_.appliesTo(violation)) match {
          case Some(rule) => (excluded :+ violation.copy(exclusionRule = Some(rule)), included)
          case None       => (excluded, included :+ violation)
        }
      }
}

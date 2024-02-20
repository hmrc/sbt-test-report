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

case class Violation(description: String, helpUrl: String, impact: String, occurrences: List[Occurrence], exclusionRules: Set[ExclusionRule] = Set.empty)

object Violation {

  private val orderByImpact: List[String] = List("critical", "serious", "moderate", "minor")
  implicit val impactOrdering: Ordering[Violation] = Ordering.by(violation => orderByImpact.indexOf(violation.impact))

  implicit class GroupedViolations(axeViolations: List[AxeViolation]) {
    def group: List[Violation] = {
      axeViolations
        .groupBy(_.help)
        .map { case (help, occurrences) =>
          Violation(
            description = help,
            helpUrl = occurrences.head.helpUrl,
            impact = occurrences.head.impact,
            exclusionRules = occurrences.flatMap(_.exclusionRule).toSet,
            occurrences = occurrences
              .groupBy(_.url)
              .map { case (url, violations) =>
                Occurrence(
                  url = url,
                  snippets = Set(violations.map(_.html) *)
                )
              }
              .toList
          )
        }
        .toList
    }
  }

}
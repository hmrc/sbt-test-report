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

object GlobalExclusionRules {

  object GovUkBackLink
      extends GlobalExclusionRule(
        maybeHtmlRegex = Some("""<a .*class="govuk-back-link.*</a>"""),
        maybePathRegex = None,
        reason =
          """Design decision by GOV.UK team - see <a href="https://github.com/alphagov/govuk-frontend/issues/1604">alphagov/govuk-frontend#1604</a>"""
      )

  object GovUkSkipLink
      extends GlobalExclusionRule(
        maybeHtmlRegex = Some("""<a .*class="govuk-skip-link.*</a>"""),
        maybePathRegex = None,
        reason =
          """Design decision by GOV.UK team - see <a href="https://github.com/alphagov/govuk-frontend/issues/1604">alphagov/govuk-frontend#1604</a>"""
      )

  val all: List[GlobalExclusionRule] = List(
    GovUkBackLink,
    GovUkSkipLink
  )
}

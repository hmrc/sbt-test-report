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

object PlatformExclusionRules {

  object GovUkBackLink
      extends PlatformExclusionRule(
        maybeHtmlRegex = Some(RegexPattern("""<a .*class="govuk-back-link.*</a>""")),
        maybePathRegex = None,
        reason =
          """Design decision by GOV.UK team - see <a href="https://github.com/alphagov/govuk-frontend/issues/1604">alphagov/govuk-frontend#1604</a>"""
      )

  object GovUkSkipLink
      extends PlatformExclusionRule(
        maybeHtmlRegex = Some(RegexPattern("""<a .*class="govuk-skip-link.*</a>""")),
        maybePathRegex = None,
        reason =
          """Design decision by GOV.UK team - see <a href="https://github.com/alphagov/govuk-frontend/issues/1604">alphagov/govuk-frontend#1604</a>"""
      )

  object AuthLoginStub
      extends PlatformExclusionRule(
        maybeHtmlRegex = None,
        maybePathRegex = Some(RegexPattern("/auth-login-stub/")),
        reason = "Dummy login service used for local testing"
      )

  object TestOnlyRoute
      extends PlatformExclusionRule(
        maybeHtmlRegex = None,
        maybePathRegex = Some(RegexPattern("/test-only/")),
        reason = "test-only routes used to configure services during local testing"
      )
  object AriaAttributesConditionalRevealRadios
      extends PlatformExclusionRule(
        maybeHtmlRegex = Some(
          RegexPattern(
            """<input .*class="govuk-radios__input".*type="radio".* aria-controls="conditional-.*".*aria-expanded=".*>"""
          )
        ),
        maybePathRegex = None,
        reason =
          """Decision by GOV.UK team - see <a href="https://github.com/alphagov/govuk-frontend/issues/979">alphagov/govuk-frontend#979</a>"""
      )

  val all: List[PlatformExclusionRule] = List(
    AuthLoginStub,
    TestOnlyRoute,
    GovUkBackLink,
    GovUkSkipLink,
    AriaAttributesConditionalRevealRadios
  )
}

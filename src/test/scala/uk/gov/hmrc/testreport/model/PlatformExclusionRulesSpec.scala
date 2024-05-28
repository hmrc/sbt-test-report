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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlatformExclusionRulesSpec extends AnyWordSpec with Matchers with ExclusionFilter {

  private val rawAxeViolations = List(
    AxeViolation(
      url = "http://localhost:12804/test-only/public-pension-adjustment/change-charges",
      help = "All page content should be contained by landmarks",
      helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
      impact = "moderate",
      html = """<a href="#">Some link</a>""",
      exclusionRules = Nil
    ),
    AxeViolation(
      url = "http://localhost:99999/auth-login-stub/some-page",
      help = "All page content should be contained by landmarks",
      helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
      impact = "moderate",
      html = """<a href="#">Some link</a>""",
      exclusionRules = Nil
    ),
    AxeViolation(
      url = "http://localhost:12804/some-service/some-page",
      help = "All page content should be contained by landmarks",
      helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
      impact = "moderate",
      html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
      exclusionRules = Nil
    ),
    AxeViolation(
      url = "http://localhost:12804/some-service/some-page",
      help = "All page content should be contained by landmarks",
      helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
      impact = "moderate",
      html = """<a href="#main-content" class="govuk-skip-link">Skip to main content</a>""",
      exclusionRules = Nil
    )
  )

  "PlatformExclusionRules.all" should {
    val (excludedViolations, includedViolations) = partitionViolations(rawAxeViolations, PlatformExclusionRules.all)

    "exclude test-only routes" in {
      excludedViolations.head.exclusionRules should be(List(PlatformExclusionRules.TestOnlyRoute))
    }

    "exclude auth-login-stub routes" in {
      excludedViolations(1).exclusionRules should be(List(PlatformExclusionRules.AuthLoginStub))
    }

    "exclude GOV.UK Back links" in {
      excludedViolations(2).exclusionRules should be(List(PlatformExclusionRules.GovUkBackLink))
    }

    "exclude GOV.UK Skip links" in {
      excludedViolations(3).exclusionRules should be(List(PlatformExclusionRules.GovUkSkipLink))
    }
  }
}

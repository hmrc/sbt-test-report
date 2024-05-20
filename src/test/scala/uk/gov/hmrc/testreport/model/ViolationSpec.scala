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

import uk.gov.hmrc.testreport.model.Violation.GroupedViolations

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ViolationSpec extends AnyWordSpec with Matchers {

  trait Setup extends ExclusionFilter {
    val axeViolations = List(
      AxeViolation(
        url = "http://localhost:12804/test-only/public-pension-adjustment/change-charges",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = List(ServiceExclusionRule(Some(RegexPattern("/test-only")), "test-only page"))
      ),
      AxeViolation(
        url = "http://localhost:12804/test-only/public-pension-adjustment/change-charges",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#main-content" class="govuk-skip-link">Skip to main content</a>""",
        exclusionRules = List(ServiceExclusionRule(Some(RegexPattern("/test-only")), "test-only page"))
      ),
      AxeViolation(
        url = "http://localhost:12804/some-other-service/some-page",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = List(ServiceExclusionRule(Some(RegexPattern("/some-other-service")), "owned by another team"))
      ),
      AxeViolation(
        url = "http://localhost:12804/some-other-service/some-page",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#main-content" class="govuk-skip-link">Skip to main content</a>""",
        exclusionRules = List(ServiceExclusionRule(Some(RegexPattern("/some-other-service")), "owned by another team"))
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/some-page",
        help = "Some other Axe violation",
        helpUrl = "https://dequeuniversity.com/rules/axe/9.9/SomeOtherRule?application=axeAPI",
        impact = "critical",
        html = """<a href="#" class="blah-blah-blah  js-visible">Do something</a>"""
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/some-other-page",
        help = "Some other Axe violation",
        helpUrl = "https://dequeuniversity.com/rules/axe/9.9/SomeOtherRule?application=axeAPI",
        impact = "critical",
        html = """<a href="#" class="blah-blah-blah  js-visible">Do something</a>"""
      )
    )
  }

  "GroupedViolations" should {
    "group violations by help text / description" in new Setup {
      axeViolations.group.map(_.description) shouldBe List(
        "All page content should be contained by landmarks",
        "Some other Axe violation"
      )
    }

    "take the helpUrl from the underlying violations" in new Setup {
      axeViolations.group.map(_.helpUrl) shouldBe List(
        "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        "https://dequeuniversity.com/rules/axe/9.9/SomeOtherRule?application=axeAPI"
      )
    }

    "take the impact from the underlying violations" in new Setup {
      axeViolations.group.map(_.impact) shouldBe List("moderate", "critical")
    }

    "group the exclusionRules from the underlying violations" in new Setup {
      axeViolations.group.map(_.exclusionRules) shouldBe List(
        Set(
          ServiceExclusionRule(Some(RegexPattern("/test-only")), "test-only page"),
          ServiceExclusionRule(Some(RegexPattern("/some-other-service")), "owned by another team")
        ),
        Set.empty
      )
    }

    "group the occurrences by url from the underlying violations" in new Setup {
      axeViolations.group.map(_.occurrences) shouldBe List(
        List(
          Occurrence(
            url = "http://localhost:12804/test-only/public-pension-adjustment/change-charges",
            snippets = Set(
              """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
              """<a href="#main-content" class="govuk-skip-link">Skip to main content</a>"""
            )
          ),
          Occurrence(
            url = "http://localhost:12804/some-other-service/some-page",
            snippets = Set(
              """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
              """<a href="#main-content" class="govuk-skip-link">Skip to main content</a>"""
            )
          )
        ),
        List(
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/some-page",
            snippets = Set("""<a href="#" class="blah-blah-blah  js-visible">Do something</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/some-other-page",
            snippets = Set("""<a href="#" class="blah-blah-blah  js-visible">Do something</a>""")
          )
        )
      )
    }
  }
}

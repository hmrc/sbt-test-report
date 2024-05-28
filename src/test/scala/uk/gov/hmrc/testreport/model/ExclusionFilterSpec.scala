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

class ExclusionFilterSpec extends AnyWordSpec with Matchers {

  trait Setup extends ExclusionFilter {
    val rawAxeViolations: List[AxeViolation] = List(
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/charges",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/multiple-schemes",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/lifetime-allowance/enhancement-type",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/pension-saving-statement",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12805/submit-public-pension-adjustment/submission-service/date-of-death-someone-else",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url =
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-pre/pension-scheme-1/pension-input-amount",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/cannot-use-service",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url = "http://localhost:12804/public-pension-adjustment/change-charges",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url =
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-pre/pension-scheme-0/charge-amount-you-paid",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      ),
      AxeViolation(
        url =
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/check-answers?someQueryParam=blah",
        help = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        html = """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
        exclusionRules = Nil
      )
    )
  }

  "ExclusionFilter" should {
    "include all violations if no rules match" in new Setup {
      val exclusionRules: List[ExclusionRule] = List(
        ServiceExclusionRule(
          maybePathRegex = Some(RegexPattern("/nothing-matched")),
          reason = "Some reason"
        )
      )

      val (exclViolations, inclViolations) = partitionViolations(rawAxeViolations, exclusionRules)
      exclViolations.length shouldBe 0
      inclViolations.length shouldBe 10
    }

    "exclude violations if rules match" in new Setup {
      val exclusionRules: List[ExclusionRule] = List(
        ServiceExclusionRule(
          maybePathRegex = Some(RegexPattern("/public-pension-adjustment/change-charges")),
          reason = "Some reason"
        ),
        ServiceExclusionRule(
          maybePathRegex = Some(RegexPattern("/public-pension-adjustment/annual-allowance")),
          reason = "Some other reason"
        )
      )

      val (exclViolations, inclViolations) = partitionViolations(rawAxeViolations, exclusionRules)
      exclViolations.length shouldBe 5
      inclViolations.length shouldBe 5

      exclViolations.map(_.exclusionRules) shouldBe List(
        List(
          ServiceExclusionRule(Some(RegexPattern("/public-pension-adjustment/annual-allowance")), "Some other reason")
        ),
        List(
          ServiceExclusionRule(Some(RegexPattern("/public-pension-adjustment/annual-allowance")), "Some other reason")
        ),
        List(ServiceExclusionRule(Some(RegexPattern("/public-pension-adjustment/change-charges")), "Some reason")),
        List(
          ServiceExclusionRule(Some(RegexPattern("/public-pension-adjustment/annual-allowance")), "Some other reason")
        ),
        List(
          ServiceExclusionRule(Some(RegexPattern("/public-pension-adjustment/annual-allowance")), "Some other reason")
        )
      )
    }

    "exclude violations based on custom regex path" in new Setup {
      val exclusionRules: List[ExclusionRule] = List(
        ServiceExclusionRule(
          maybePathRegex =
            Some(RegexPattern("/public-pension-adjustment/annual-allowance/[0-9]{4}-pre/pension-scheme-[0-9]")),
          reason = "Some other reason"
        )
      )

      val (exclViolations, inclViolations) = partitionViolations(rawAxeViolations, exclusionRules)
      exclViolations.length shouldBe 2
      inclViolations.length shouldBe 8
    }

    "require exclusion paths containing special characters to be escaped" in new Setup {
      val exclusionRules: List[ExclusionRule] = List(
        ServiceExclusionRule(
          maybePathRegex = Some(RegexPattern("/check-answers\\?someQueryParam=blah")),
          reason = "Some other reason"
        )
      )

      val (exclViolations, inclViolations) = partitionViolations(rawAxeViolations, exclusionRules)
      exclViolations.length shouldBe 1
      inclViolations.length shouldBe 9
    }

    "exclude violations defined in Platform Exclusion Rules" in new Setup {
      val (exclViolations, inclViolations) = partitionViolations(rawAxeViolations, PlatformExclusionRules.all)
      exclViolations.length shouldBe 10
      inclViolations        shouldBe empty
    }
  }
}

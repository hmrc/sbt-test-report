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

package uk.gov.hmrc.testreport.report

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.testreport.model.{BuildDetails, Occurrence, PlatformExclusionRules, RegexPattern, ServiceExclusionRule, Violation}
import uk.gov.hmrc.testreport.report.AccessibilityReport.htmlReport

import scala.jdk.CollectionConverters.*
import java.time.{ZoneId, ZonedDateTime}

class AccessibilityReportSpec extends AnyWordSpec with Matchers {

  trait Setup {
    val projectName        = "service-a"
    val jenkinsBuildId     = "101"
    val jenkinsBuildUrl    = "http://test.com/build/101/"
    val isJenkinsBuild     = true
    val browser            = "Chrome"
    val htmlReportFilename = "report.html"
    val creationDate       = ZonedDateTime.of(2000, 12, 1, 12, 0, 0, 0, ZoneId.of("GMT"))

    val buildDetails: BuildDetails =
      BuildDetails(
        projectName,
        jenkinsBuildId,
        jenkinsBuildUrl,
        isJenkinsBuild,
        browser,
        htmlReportFilename,
        creationDate
      )

    val includedViolations: List[Violation] = List(
      Violation(
        description = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
        impact = "moderate",
        occurrences = List(
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/cannot-use-service",
            snippets = Set(
              """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
              """<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">"""
            )
          ),
          Occurrence(
            url =
              "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/pension-scheme-0/charge-amount-you-paid",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url =
              "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-pre/pension-scheme-0/pension-input-amount",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url =
              "http://localhost:12805/submit-public-pension-adjustment/submission-service/date-of-death-someone-else",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/lifetime-allowance/enhancement-type",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/charges",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/pension-saving-statement",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/check-answers",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/multiple-schemes",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          ),
          Occurrence(
            url = "http://localhost:12804/public-pension-adjustment/change-charges",
            snippets = Set("""<a href="#" class="govuk-back-link  js-visible">Back</a>""")
          )
        )
      ),
      Violation(
        description = "Select element must have an accessible name",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/select-name?application=axeAPI",
        impact = "critical",
        occurrences = List(
          Occurrence(
            url = "http://localhost:9017/whats-running-where",
            snippets =
              Set("""<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">""")
          ),
          Occurrence(
            url = "http://localhost:9017/config/search",
            snippets = Set("""<select class="form-select rounded-start-0" name="valueFilterType">""")
          )
        )
      )
    )

    val testOnlyRouteExclusion     = ServiceExclusionRule(Some(RegexPattern("/test-only")), "only used for testing")
    val authLoginStubExclusionRule =
      ServiceExclusionRule(Some(RegexPattern("/auth-stub")), "auth stub is maintained by another service team")

    val excludedViolations: List[Violation] = List(
      Violation(
        description = "Select element must have an accessible name",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/select-name?application=axeAPI",
        impact = "critical",
        occurrences = List(
          Occurrence(
            url = "http://localhost:9017/test-only",
            snippets =
              Set("""<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">""")
          ),
          Occurrence(
            url = "http://localhost:9017/test-only/path/to",
            snippets = Set("""<select class="form-select rounded-start-0" name="valueFilterType">""")
          )
        ),
        exclusionRules = Set(
          testOnlyRouteExclusion
        )
      ),
      Violation(
        description = "Document should have one main landmark",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.8/select",
        impact = "moderate",
        occurrences = List(
          Occurrence(
            url = "http://localhost:9017/auth-stub",
            snippets =
              Set("""<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">""")
          ),
          Occurrence(
            url = "http://localhost:9017/auth-stub/path/to",
            snippets = Set("""<select class="form-select rounded-start-0" name="valueFilterType">""")
          )
        ),
        exclusionRules = Set(
          testOnlyRouteExclusion,
          authLoginStubExclusionRule
        )
      ),
      Violation(
        description = "All page content should be contained by landmarks",
        helpUrl = "https://dequeuniversity.com/rules/axe/4.9/region?application=axeAPI",
        impact = "moderate",
        occurrences = List(
          Occurrence(
            url = "http://localhost:1234/my-service",
            snippets = Set(
              """<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>"""
            )
          )
        ),
        exclusionRules = Set(
          PlatformExclusionRules.GovUkSkipLink
        )
      )
    )

    lazy val reportHtml: Document = {
      val html: String = htmlReport(buildDetails, includedViolations, excludedViolations)
      Jsoup.parse(html)
    }
  }

  "AccessibilityReport" when {
    "search prompt" in new Setup {
      reportHtml.getElementById("summary").text() shouldBe "Displaying 2 outstanding violations."
    }

    "render report footer" in new Setup {
      val footerInfo: String =
        reportHtml.body().getElementsByTag("footer").first().getElementsByTag("p").first().html()
      footerInfo shouldBe s"""© 2023 <a href="https://github.com/hmrc/$projectName" target="_blank" rel="noreferrer noopener">$projectName</a>"""
    }

    "built on Jenkins" should {
      "render report header with Jenkins build details" in new Setup {
        val headMetaIssuesCount: String =
          reportHtml.head().getElementsByAttributeValue("name", "description").attr("content")
        val headTitle: String           = reportHtml.head().getElementsByTag("title").first().html()
        val headerInfo: String          =
          reportHtml.body().getElementsByTag("header").first().getElementsByTag("p").first().html()
        headMetaIssuesCount shouldBe s"${includedViolations.length} violations identified."
        headTitle           shouldBe s"Accessibility assessment for $projectName"
        headerInfo          shouldBe s"""<a href="$jenkinsBuildUrl" target="_parent">#$jenkinsBuildId</a> of <a href="https://github.com/hmrc/$projectName" target="_blank" rel="noreferrer noopener">service-a</a> on <time datetime="2000-12-01T12:00:00Z">1 December 2000, 12:00:00 GMT</time> (Chrome)"""
      }
      "render report feedback banner present" in new Setup {
        val feedbackLinkBanner: String =
          reportHtml.body().getElementsByClass("feedback").first().getElementsByTag("p").first().html()
        feedbackLinkBanner shouldBe s"""If you have any feedback on using this report, we would love to hear from you. <a href="https://forms.gle/T39z8o6rjfLyHym99" target="_blank" rel="noreferrer noopener">Provide Feedback</a>"""
      }
    }

    "built locally" should {
      "render report header with local build details" in new Setup {
        override val isJenkinsBuild = false

        val headMetaIssuesCount: String =
          reportHtml.head().getElementsByAttributeValue("name", "description").attr("content")
        val headTitle: String           = reportHtml.head().getElementsByTag("title").first().html()
        val headerInfo: String          =
          reportHtml.body().getElementsByTag("header").first().getElementsByTag("p").first().html()
        headMetaIssuesCount shouldBe s"${includedViolations.length} violations identified."
        headTitle           shouldBe s"Accessibility assessment for $projectName"
        headerInfo          shouldBe s"""Local build of <a href="https://github.com/hmrc/$projectName" target="_blank" rel="noreferrer noopener">$projectName</a> on <time datetime="2000-12-01T12:00:00Z">1 December 2000, 12:00:00 GMT</time> (Chrome)"""
      }
      "render report feedback banner present" in new Setup {
        override val isJenkinsBuild = false

        val feedbackLinkBanner: String =
          reportHtml.body().getElementsByClass("feedback").first().getElementsByTag("p").first().html()
        feedbackLinkBanner shouldBe s"""If you have any feedback on using this report, we would love to hear from you. <a href="https://forms.gle/T39z8o6rjfLyHym99" target="_blank" rel="noreferrer noopener">Provide Feedback</a>"""
      }
    }

    "there are outstanding violations" should {
      "render a card for each violation" in new Setup {
        val violations: Element = reportHtml.body().getElementById("violations")
        violations.getElementsByClass("card-violation").size() shouldBe 2
      }

      "show the Axe rule as the card heading" in new Setup {
        val violations: Element = reportHtml.body().getElementById("violations")
        violations.getElementsByTag("h2").asScala.toList.map(_.text) shouldBe List(
          "All page content should be contained by landmarks",
          "Select element must have an accessible name"
        )
      }

      "show the Axe impact as a tag" in new Setup {
        val violations: Element = reportHtml.body().getElementById("violations")
        violations.getElementsByClass("tag").asScala.toList.map(_.text) shouldBe List("moderate", "critical")
      }

      "show a link to the Axe help page for the rule" in new Setup {
        val violations: Element = reportHtml.body().getElementById("violations")
        violations.getElementsByClass("axe-rule-url").asScala.toList.map(_.attr("href")) shouldBe List(
          "https://dequeuniversity.com/rules/axe/4.8/region?application=axeAPI",
          "https://dequeuniversity.com/rules/axe/4.8/select-name?application=axeAPI"
        )
      }

      "show a list of affected URLs with affected snippets from each URL" in new Setup {
        val violations: Element = reportHtml.body().getElementById("violations")
        violations.getElementsByClass("occurrence-axe-rule-url").asScala.toList.map(_.attr("href")) shouldBe List(
          "http://localhost:12804/public-pension-adjustment/cannot-use-service",
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/pension-scheme-0/charge-amount-you-paid",
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-pre/pension-scheme-0/pension-input-amount",
          "http://localhost:12805/submit-public-pension-adjustment/submission-service/date-of-death-someone-else",
          "http://localhost:12804/public-pension-adjustment/lifetime-allowance/enhancement-type",
          "http://localhost:12804/public-pension-adjustment/charges",
          "http://localhost:12804/public-pension-adjustment/pension-saving-statement",
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/check-answers",
          "http://localhost:12804/public-pension-adjustment/annual-allowance/2016-post/multiple-schemes",
          "http://localhost:12804/public-pension-adjustment/change-charges",
          "http://localhost:9017/whats-running-where",
          "http://localhost:9017/config/search"
        )

        violations
          .getElementsByClass("occurrence")
          .first()
          .getElementsByTag("pre")
          .asScala
          .toList
          .map(_.text) shouldBe List(
          """<a href="#" class="govuk-back-link  js-visible">Back</a>""",
          """<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">"""
        )
      }
    }

    "there are excluded violations" should {
      "render a card for each excluded path" in new Setup {
        val violations: Element = reportHtml.body().getElementById("exclusions")
        violations.getElementsByClass("card").size() shouldBe 3
      }

      "show the Axe rule as the card heading" in new Setup {
        val violations: Element = reportHtml.body().getElementById("exclusions")
        violations.getElementsByTag("h2").asScala.toList.map(_.text) shouldBe List(
          "Select element must have an accessible name",
          "Document should have one main landmark",
          "All page content should be contained by landmarks"
        )
      }

      "show the Axe impact as a tag" in new Setup {
        val violations: Element = reportHtml.body().getElementById("exclusions")
        violations.getElementsByClass("tag").asScala.toList.map(_.text) shouldBe List(
          "critical",
          "moderate",
          "moderate"
        )
      }

      "show a table of each excluded rules' filter type, path, HTML and reason" in new Setup {
        val exclusions: Element = reportHtml.body().getElementById("exclusions")
        val th                  = exclusions.getElementsByTag("th").asScala.toList.map(_.text)
        val td                  = exclusions.getElementsByTag("td").asScala.toList.map(_.text)

        th.zip(td) shouldBe List(
          ("Excluded by", "Service"),
          ("When path matches", "/test-only"),
          ("Reason", "only used for testing"),
          ("Excluded by", "Service"),
          ("When path matches", "/test-only"),
          ("Reason", "only used for testing"),
          ("Excluded by", "Service"),
          ("When path matches", "/auth-stub"),
          ("Reason", "auth stub is maintained by another service team"),
          ("Excluded by", "Platform"),
          ("When HTML matches", """<a .*class="govuk-skip-link.*</a>"""),
          ("Reason", "Design decision by GOV.UK team - see alphagov/govuk-frontend#1604")
        )
      }

      "show a list of affected URLs with affected snippets from each URL" in new Setup {
        val violations: Element = reportHtml.body().getElementById("exclusions")
        violations.getElementsByClass("occurrence-axe-rule-url").asScala.toList.map(_.attr("href")) shouldBe List(
          "http://localhost:9017/test-only",
          "http://localhost:9017/test-only/path/to",
          "http://localhost:9017/auth-stub",
          "http://localhost:9017/auth-stub/path/to",
          "http://localhost:1234/my-service"
        )

        violations
          .getElementsByClass("occurrence")
          .first()
          .getElementsByTag("pre")
          .asScala
          .toList
          .map(_.text) shouldBe List(
          """<select class="form-select" id="profile_name" name="profile_name" style="width: 200px">"""
        )
      }
    }
  }
}

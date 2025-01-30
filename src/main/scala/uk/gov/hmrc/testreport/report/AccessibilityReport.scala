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

import scalatags.Text
import scalatags.Text.all.*
import scalatags.Text.tags2.{article, details, nav, summary, time}
import uk.gov.hmrc.testreport.model.{BuildDetails, Violation}

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoUnit
import java.util.Locale

object AccessibilityReport {

  def htmlReport(
    buildDetails: BuildDetails,
    includedViolations: List[Violation],
    excludedViolations: List[Violation]
  ): String = "<!DOCTYPE html>" + html(
    htmlHead(buildDetails, includedViolations.length),
    body(
      reportHeader(buildDetails),
      feedbackLink,
      tag("main")(
        violations(includedViolations),
        exclusions(excludedViolations)
      ),
      htmlFooter(buildDetails)
    )
  )

  private def htmlHead(buildDetails: BuildDetails, includedViolationCount: Int): Text.TypedTag[String] =
    head(
      meta(charset := "utf-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1"),
      tag("title")(s"Accessibility assessment for ${buildDetails.projectName}"),
      link(rel := "stylesheet", href := "css/report.css"),
      meta(
        name := "description",
        if (includedViolationCount == 0) content := "No violations identified."
        else content := s"$includedViolationCount violations identified."
      )
    )

  private def reportHeader(buildDetails: BuildDetails): Text.TypedTag[String] =
    header(
      cls := "border-bottom",
      role := "banner",
      div(
        cls := "banner",
        div(
          cls := "region wrapper",
          p(
            if (buildDetails.isJenkinsBuild)
              a(href := buildDetails.jenkinsBuildUrl, target := "_parent", s"#${buildDetails.jenkinsBuildId}")
            else "Local build",
            " of ",
            a(
              href := s"https://github.com/hmrc/${buildDetails.projectName}",
              target := "_blank",
              rel := "noreferrer noopener",
              buildDetails.projectName
            ),
            " on ",
            time(
              attr("datetime") := htmlDateTime(buildDetails.creationDateTime),
              readableDateTime(buildDetails.creationDateTime)
            ),
            s" (${buildDetails.browser})"
          )
        )
      )
    )

  private def feedbackLink: Text.TypedTag[String] =
    div(
      cls := "feedback",
      role := "region",
      attr("aria-label") := "feedback",
      p(
        textAlign := "center",
        "If you have any feedback on using this report, we would love to hear from you. ",
        a(
          href := "https://forms.gle/tHG7WYXtpdBuxHNF7",
          target := "_blank",
          rel := "noreferrer noopener",
          "Provide Feedback"
        )
      )
    )

  private def htmlDateTime(zonedDateTime: ZonedDateTime): String =
    zonedDateTime.truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT)

  private def readableDateTime(zonedDateTime: ZonedDateTime): String = {
    val formatter = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.LONG)
      .withLocale(Locale.UK)
    zonedDateTime.format(formatter)
  }

  private def violations(includedViolations: List[Violation]): Text.TypedTag[String] =
    article(
      cls := "flow region wrapper no-padding-bottom",
      div(
        cls := "heading",
        h1("Accessibility assessment")
      ),
      h2("Outstanding Violations"),
      p(
        id := "summary",
        attr("aria-live") := "assertive",
        if (includedViolations.isEmpty) "No violations identified."
        else
          s"Displaying ${includedViolations.length} outstanding ${if (includedViolations.length > 1) "violations"
            else "violation"}."
      ),
      cards(includedViolations, "violations", "card-violation")
    )

  private def exclusions(excludedViolations: List[Violation]): Text.TypedTag[String] =
    article(
      cls := "flow region wrapper",
      h2("Excluded Violations"),
      p(
        id := "summary",
        attr("aria-live") := "assertive",
        if (excludedViolations.isEmpty) "No violations were excluded."
        else
          s"Displaying ${excludedViolations.length} excluded ${if (excludedViolations.length > 1) "violations"
            else "violation"}."
      ),
      cards(excludedViolations, "exclusions")
    )

  private def cards(violations: List[Violation], identity: String, classes: String = ""): Text.TypedTag[String] =
    div(
      cls := "report",
      ul(
        id := identity,
        cls := "flow",
        role := "list",
        violations.map { violation =>
          val helpUrl      = violation.helpUrl
          val occurrences  = violation.occurrences
          val urlCount     = occurrences.length
          val elementCount = occurrences.map(occurrence => occurrence.snippets.toList.length).sum

          li(
            article(
              cls := s"card $classes border",
              header(
                cls := "repel region",
                h2(violation.description),
                impactTag(violation.impact)
              ),
              dl(
                cls := "border-top",
                if (violation.isExcluded) {
                  violation.exclusionRules.toList.map { rule =>
                    div(
                      cls := "border-bottom flow region",
                      table(
                        tbody(
                          tr(
                            th(attr("scope") := "row", "Excluded by"),
                            td(rule.scope)
                          ),
                          rule.maybePathRegex.map { pathRegex =>
                            tr(
                              th(attr("scope") := "row", "When path matches"),
                              td(pathRegex.raw.toString)
                            )
                          },
                          rule.maybeHtmlRegex.map { htmlRegex =>
                            tr(
                              th(attr("scope") := "row", "When HTML matches"),
                              td(htmlRegex.raw.toString)
                            )
                          },
                          tr(
                            th(attr("scope") := "row", "Reason"),
                            td(raw(rule.reason))
                          )
                        )
                      )
                    )
                  }
                } else {
                  div(
                    cls := "border-bottom flow region",
                    dt("Documentation"),
                    dd(
                      a(
                        cls := "axe-rule-url",
                        href := helpUrl,
                        target := "_blank",
                        rel := "noreferrer noopener",
                        helpUrl
                      )
                    )
                  )
                },
                div(
                  cls := "flow region",
                  dt("Affected"),
                  dd(
                    details(
                      summary(s"$elementCount elements affected across $urlCount pages"),
                      ul(
                        cls := "flow region",
                        occurrences.map { occurrence =>
                          li(
                            cls := "occurrence flow",
                            a(
                              cls := "occurrence-axe-rule-url",
                              href := occurrence.url,
                              target := "_blank",
                              occurrence.url
                            ),
                            ul(
                              cls := "flow",
                              role := "list",
                              occurrence.snippets.map(html => li(pre(html))).toList
                            )
                          )
                        }
                      )
                    )
                  )
                )
              )
            )
          )
        }
      )
    )

  private def impactTag(impact: String): Text.TypedTag[String] =
    span(
      cls := "tag",
      attr("data-impact") := impact,
      attr("aria-label") := s"Violation impact is $impact",
      impact
    )

  private def htmlFooter(buildDetails: BuildDetails): Text.TypedTag[String] =
    footer(
      cls := "border-top",
      role := "contentinfo",
      div(
        cls := "repel region wrapper",
        p(
          "© 2023 ",
          a(
            href := s"https://github.com/hmrc/${buildDetails.projectName}",
            target := "_blank",
            rel := "noreferrer noopener",
            buildDetails.projectName
          )
        ),
        nav(
          attr("aria-label") := "secondary navigation",
          ul(
            cls := "cluster",
            role := "list",
            li(
              a(
                href := "https://github.com/hmrc/accessibility/blob/main/README.md",
                target := "_blank",
                rel := "noreferrer noopener",
                "Guidance"
              )
            ),
            li(
              a(
                href := "https://hmrcdigital.slack.com/archives/C4JQESR8U",
                target := "_blank",
                rel := "noreferrer noopener",
                "Support"
              )
            ),
            li(
              a(
                href := "https://forms.gle/T39z8o6rjfLyHym99",
                target := "_blank",
                rel := "noreferrer noopener",
                "Feedback"
              )
            ),
            li(
              a(href := "#", "Back to top", target := "_parent")
            )
          )
        )
      )
    )

}

/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.testreport

import sbt.*
import scalatags.Text.all.*
import scalatags.Text.svgTags.{path, svg}
import scalatags.Text.tags2.{article, details, nav, summary, time}

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.temporal.ChronoUnit
import scala.Console.{GREEN, RED, RESET}

object TestReportPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val testReport          = taskKey[Unit]("generate test report")
    val testReportDirectory = settingKey[File]("test report directory")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    testReportDirectory := Keys.target.value / "test-reports"
  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val axeResultsDirectory = os.Path(testReportDirectory.value / "accessibility-assessment" / "axe-results")
    val logger              = sbt.Keys.streams.value.log

    if (os.exists(axeResultsDirectory)) {
      logger.info("Analysing accessibility assessment results ...")

      val projectName         = Keys.name.value
      val isJenkinsBuild      = sys.env.contains("BUILD_ID")
      val jenkinsBuildId      = sys.env.getOrElse("BUILD_ID", "BUILD_ID")
      val jenkinsBuildUrl     = sys.env.getOrElse("BUILD_URL", "BUILD_URL")
      val browser             = sys.props.getOrElse("browser", "BROWSER").capitalize
      val htmlReportDirectory = testReportDirectory.value / "accessibility-assessment" / "html-report"
      val htmlReport          = htmlReportDirectory / "index.html"

      // Copy styles
      os.makeDir.all(os.Path(htmlReportDirectory / "css"))
      os.write.over(
        os.Path(htmlReportDirectory / "css" / "style.min.css"),
        os.read(os.resource(getClass.getClassLoader) / "assets" / "styles" / "style.min.css")
      )

      // Copy scripts
      os.makeDir.all(os.Path(htmlReportDirectory / "js"))
      os.write.over(
        os.Path(htmlReportDirectory / "js" / "search.min.js"),
        os.read(os.resource(getClass.getClassLoader) / "assets" / "scripts" / "search.min.js")
      )

      // Get all axe violations
      val axeViolationsAll = for {
        reportDir <- os.list.stream(axeResultsDirectory).filter(os.isDir)
        reportJson = ujson.read(os.read(reportDir / "axeResults.json"))
        violation <- reportJson("violations").arr
        snippet   <- violation("nodes").arr
      } yield Map(
        "url"     -> reportJson("url").str,
        "help"    -> violation("help").str,
        "helpUrl" -> violation("helpUrl").str,
        "impact"  -> violation("impact").str,
        "html"    -> snippet("html").str
      )

      // Group and deduplicate all axe violations, then order violations by impact
      case class Occurrence(url: String, snippets: Set[String])
      case class Violation(help: String, helpUrl: String, impact: String, occurrences: List[Occurrence])

      val axeViolationsFiltered = axeViolationsAll.toList
        .groupBy(_("help"))
        .map { case (help, occurrences) =>
          Violation(
            help = help,
            helpUrl = occurrences.head("helpUrl"),
            impact = occurrences.head("impact"),
            occurrences = occurrences
              .groupBy(_("url"))
              .map { case (url, issues) =>
                Occurrence(
                  url = url,
                  snippets = Set(issues.map(_("html")) *)
                )
              }
              .toList
          )
        }
        .toList

      val orderByImpact = List("critical", "serious", "moderate", "minor")

      val axeViolations = axeViolationsFiltered.sortBy(violation => orderByImpact.indexOf(violation.impact))

      // Get total axe violations count
      val axeViolationsCount = axeViolations.length

      if (axeViolationsCount > 0) {
        logger.error(s"${RED}Accessibility assessment: $axeViolationsCount violations$RESET")
      } else {
        logger.info(s"${GREEN}Accessibility assessment: $axeViolationsCount violations$RESET")
      }

      // Get current datetime
      val htmlDateTime     = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT)
      val readableDateTime = ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))

      // Write HTML document
      logger.info("Writing accessibility assessment report ...")
      os.write.over(
        os.Path(htmlReport),
        "<!DOCTYPE html>" + html(
          head(
            meta(charset := "utf-8"),
            meta(name := "viewport", content := "width=device-width, initial-scale=1"),
            tag("title")(s"Accessibility assessment for $projectName"),
            link(rel := "stylesheet", href := "css/style.min.css"),
            meta(
              name := "description",
              if (axeViolationsCount == 0) content := "No issues identified."
              else content := s"$axeViolationsCount issues identified."
            )
          ),
          body(
            header(
              cls := "border-bottom",
              role := "banner",
              div(
                cls := "banner",
                div(
                  cls := "region wrapper",
                  p(
                    if (isJenkinsBuild) a(href := jenkinsBuildUrl, target := "_parent", s"#$jenkinsBuildId")
                    else "Local build",
                    " of ",
                    a(
                      href := s"https://github.com/hmrc/$projectName",
                      target := "_blank",
                      rel := "noreferrer noopener",
                      projectName
                    ),
                    " on ",
                    time(attr("datetime") := htmlDateTime, readableDateTime),
                    s" ($browser)"
                  )
                )
              ),
              div(
                cls := "repel region wrapper",
                a(
                  cls := "brand",
                  if (isJenkinsBuild) href := s"${jenkinsBuildUrl}Accessibility_20Assessment_20Report/"
                  else href := s"$htmlReport",
                  target := "_parent",
                  attr("aria-label") := "Accessibility assessment",
                  svg(
                    width := "100",
                    height := "100",
                    attr("viewBox") := "0 0 100 100",
                    attr("fill") := "currentColor",
                    xmlns := "http://www.w3.org/2000/svg",
                    path(
                      attr("d") := "M50,0 C77.6142375,0 100,22.3857625 100,50 C100,77.6142375 77.6142375,100 50,100 C22.3857625,100 0,77.6142375 0,50 C0,22.3857625 22.3857625,0 50,0 Z M50,9.1796875 C27.4555639,9.1796875 9.1796875,27.4555639 9.1796875,50 C9.1796875,72.5444361 27.4555639,90.8203125 50,90.8203125 C72.5444361,90.8203125 90.8203125,72.5444361 90.8203125,50 C90.8203125,27.4555639 72.5444361,9.1796875 50,9.1796875 Z M50,16.9921875 C68.2297115,16.9921875 83.0078125,31.7702885 83.0078125,50 C83.0078125,68.2297115 68.2297115,83.0078125 50,83.0078125 C31.7702885,83.0078125 16.9921875,68.2297115 16.9921875,50 C16.9921875,31.7702885 31.7702885,16.9921875 50,16.9921875 Z M66.796875,39.453125 L33.203125,39.453125 C31.0457627,39.453125 29.296875,41.2020127 29.296875,43.359375 C29.296875,45.5167373 31.0457627,47.265625 33.203125,47.265625 L46.2890625,47.265625 L46.2890625,55.0792969 L35.2174276,69.250252 C33.8892228,70.9502767 34.1906423,73.4051418 35.890667,74.7333467 C37.5906917,76.0615515 40.0455568,75.760132 41.3737617,74.0601073 L50.1,62.890625 L50.234375,62.890625 L58.9608158,74.0601073 C60.2890207,75.760132 62.7438858,76.0615515 64.4439105,74.7333467 C66.1439352,73.4051418 66.4453547,70.9502767 65.1171498,69.250252 L54.1015625,55.1511719 L54.1015625,47.265625 L66.796875,47.265625 C68.9542373,47.265625 70.703125,45.5167373 70.703125,43.359375 C70.703125,41.2020127 68.9542373,39.453125 66.796875,39.453125 Z M50.0976562,23.828125 C46.8076787,23.828125 44.140625,26.4951787 44.140625,29.7851562 C44.140625,33.0751338 46.8076787,35.7421875 50.0976562,35.7421875 C53.3876338,35.7421875 56.0546875,33.0751338 56.0546875,29.7851562 C56.0546875,26.4951787 53.3876338,23.828125 50.0976562,23.828125 Z"
                    )
                  )
                ),
                nav(
                  attr("aria-label") := "primary navigation",
                  form(
                    id := "form",
                    cls := "visually-hidden",
                    input(id := "search", tpe := "search", name := "search", placeholder := "Search...")
                  )
                )
              )
            ),
            tag("main")(
              article(
                cls := "flow region wrapper",
                h1("Accessibility assessment"),
                p(
                  id := "summary",
                  attr("aria-live") := "assertive",
                  if (axeViolationsCount == 0) "No issues identified."
                  else s"Displaying $axeViolationsCount issues identified."
                ),
                div(
                  cls := "report",
                  ul(
                    id := "accessibility-assessment",
                    cls := "flow",
                    role := "list",
                    axeViolations.map { violation =>
                      val impact       = violation.impact
                      val help         = violation.help
                      val helpUrl      = violation.helpUrl
                      val occurrences  = violation.occurrences
                      val urlCount     = occurrences.length
                      val elementCount = occurrences.map(occurrence => occurrence.snippets.toList.length).sum

                      li(
                        article(
                          cls := "card border",
                          header(
                            cls := "repel region",
                            h2(help),
                            span(
                              cls := "tag",
                              attr("data-impact") := impact,
                              attr("aria-label") := s"Issue impact is $impact",
                              impact
                            )
                          ),
                          dl(
                            cls := "border-top",
                            div(
                              cls := "border-bottom flow region",
                              dt("Documentation"),
                              dd(
                                a(
                                  href := helpUrl,
                                  target := "_blank",
                                  rel := "noreferrer noopener",
                                  helpUrl
                                )
                              )
                            ),
                            div(
                              cls := "flow region",
                              dt("Affected"),
                              dd(
                                details(
                                  summary(s"$elementCount elements affected across $urlCount pages"),
                                  ul(
                                    cls := "flow region",
                                    occurrences.map(occurrence =>
                                      li(
                                        cls := "flow",
                                        a(
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
                                    )
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
              )
            ),
            footer(
              cls := "border-top",
              role := "contentinfo",
              div(
                cls := "repel region wrapper",
                p(
                  "© 2023 ",
                  a(
                    href := s"https://github.com/hmrc/$projectName",
                    target := "_blank",
                    rel := "noreferrer noopener",
                    projectName
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
            ),
            script(src := "js/search.min.js")
          )
        )
      )

      // Write axe violations count file
      val axeViolationsCountFile = axeResultsDirectory / "axeViolationsCount.json"
      os.write.over(axeViolationsCountFile, axeViolationsCount.toString)

      if (isJenkinsBuild) {
        logger.info(s"Wrote accessibility assessment report to ${jenkinsBuildUrl}Accessibility_20Assessment_20Report/ ")
      } else {
        logger.info(s"Wrote accessibility assessment report to file://$htmlReport")
      }
    } else {
      logger.error("No accessibility assessment results to analyse.")
    }
  }

}

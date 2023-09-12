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

import sbt.{Def, *}

object TestReportPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val testReport      = taskKey[Unit]("generate test report")
    val reportDirectory = settingKey[File]("output directory")
    val htmlReport      = settingKey[File]("output file")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    reportDirectory := (Keys.target.value / "test-reports" / "accessibility-assessment"),
    htmlReport := (reportDirectory.value / "html-report" / "index.html")
  )

  //  def headerFrag(projectName: String, browserUserAgent: String) = header(
//    cls := "border-bottom",
//    role := "banner",
//    div(
//      cls := "banner region wrapper",
//      p(
//        "Generated from ",
//        if (isJenkinsBuild) a(href := jenkinsBuildUrl, jenkinsBuildId)
//        else "local build",
//        " of ",
//        if (browserUserAgent.contains("Edg/")) "(Edge) "
//        else "(Chrome) ",
//        a(href := s"https://github.com/hmrc/$projectName", projectName),
//        s" on  {{ timestamp }}"
//      )
//    ),
//    div(
//      cls := "repel region wrapper",
//      tag("nav")(
//        aria.label := "primary navigation",
//        ul(
//          cls := "cluster",
//          role := "list",
//          li(
//            a(href := "#", target := "_blank", "Guidance")
//          ),
//          li(
//            a(href := "#", target := "_blank", "Support")
//          ),
//          li(
//            a(href := "#", target := "_blank", "Feedback")
//          )
//        )
//      )
//    )
//  )

//  def footerFrag(projectName: String) = footer(
//    cls := "border-top repel region wrapper",
//    role := "contentinfo",
//    p("© 2023 ", a(href := s"https://github.com/hmrc/$projectName", projectName)),
//    tag("nav")(
//      aria.label := "secondary navigation",
//      ul(
//        cls := "cluster",
//        role := "list",
//        li(
//          a(href := "#", "Back to top")
//        )
//      )
//    )
//  )
//
//  def sidebarFrag =
//    tag("aside")(
//      id := "sidebar",
//      form(
//        id := "form",
//        cls := "flow",
//        scalaLabel(forAttribute := "search", "Search"),
//        input(id := "search", cls := "search", tpe := "text", name := "search"),
//        fieldset(
//          cls := "flow",
//          legend("Impact"),
//          ul(
//            cls := "flow",
//            role := "list",
//            aria.live := "assertive",
//            li(
//              cls := "cluster",
//                input(id := "impact-critical", tpe := "checkbox", name := "impact", value := "critical"),
//                scalaLabel(forAttribute := "impact-critical", "Critical")
//            ),
//            li(
//              cls := "cluster",
//              input(id := "impact-serious", tpe := "checkbox", name := "impact", value := "serious"),
//              scalaLabel(`for` := "impact-serious", "Serious")
//            ),
//            li(
//              cls := "cluster",
//              input(id := "impact-moderate", tpe := "checkbox", name := "impact", value := "moderate"),
//              scalaLabel(`for` := "impact-moderate", "Moderate")
//            ),
//            li(
//              cls := "cluster",
//              input(id := "impact-info", tpe := "checkbox", name := "impact", value := "info"),
//              scalaLabel(`for` := "impact-info", "Info")
//            )
//          )
//        ),
//        button(id := "clear", "Clear")
//      )
//    )

//  def reportFrag(axeResultsViolationCount: String) = tag("article")(
//    cls := "flow",
//    h1("Accessibility assessment"),
//    p(
//      aria.live := "assertive",
//      id := "issueCount",
//      s"Displaying $axeResultsViolationCount of $axeResultsViolationCount issues identified."
//    ),
//    ul(
//      issueFrag
//    )
//  )
//  def issueFrag                                    = li(
//    data("impact") := "serious",
//    data("hash") := "ABC456",
//    tag("article")(
//      cls := "summary border",
//      header(
//        cls := "repel border-bottom region wrapper",
//        h2(
//          a(
//            cls := "id",
//            href := "https://dequeuniversity.com/rules/axe/4.7/region?application=axeAPI",
//            target := "_blank",
//            "region"
//          )
//        ),
//        ul(
//          cls := "cluster",
//          role := "list",
//          li(
//            span(cls := "impact tag", data("tag") := "serious", "serious")
//          ),
//          li(
//            span(cls := "version tag", data("tag") := "version", "4.7.2")
//          )
//        )
//      ),
//      div(
//        cls := "wrapper",
//        div(cls := "border-bottom region"),
//        dl(
//          dt("Help"),
//          dd(cls := "help", "All page content should be contained by landmarks")
//        )
//      ),
//      div(
//        cls := "border-bottom region",
//        dl(
//          dt("HTML"),
//          dd(
//            pre(
//              cls := "html",
//              """<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>"""
//            )
//          )
//        )
//      ),
//      div(
//        cls := "border-bottom region",
//        dl(
//          dt("Affects"),
//          dd(
//            tag("details")(
//              tag("summary")("2 URLs"),
//              ul(
//                cls := "affects",
//                li(
//                  a(href := "#", "http://localhost:9080/check-your-vat-flat-rate/vat-return-period")
//                ),
//                li(
//                  a(href := "#", "http://localhost:9080/check-your-vat-flat-rate/turnover")
//                )
//              )
//            )
//          )
//        )
//      ),
//      div(
//        cls := "region",
//        dl(
//          dt("Permalink"),
//          dd(
//            a(
//              cls := "permalink",
//              href := "http://localhost:8080/?search=ABC456",
//              "http://localhost:8080/?search=ABC456"
//            )
//          )
//        )
//      )
//    )
//  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val log                 = sbt.Keys.streams.value.log
    val axeResultsDirectory = os.Path(reportDirectory.value / "axe-results")

    def hasAxeResults: Boolean = os.exists(axeResultsDirectory)

    if (hasAxeResults) {
      log.info("Generating accessibility assessment report ...")
      os.makeDir.all(os.Path(reportDirectory.value / "html-report" / "assets"))

      val assets = List(
        "list.min.js",
        "style.css"
      )

      val htmlReport = "index.html"

      assets.foreach { fileName =>
        os.write.over(
          os.Path(reportDirectory.value / "html-report" / "assets" / fileName),
          os.read(os.resource(getClass.getClassLoader) / "assets" / fileName)
        )
      }

      val axeResults = os.list
        .stream(axeResultsDirectory)
        .filter(os.isDir)
        .map { timestampDirectory =>
          val ujsonValue = ujson.read(os.read(timestampDirectory / "axeResults.json"))
          ujson.write(ujsonValue, indent = 4)
        }
        .mkString(",")

      // TODO: Set reportMetaData
      val projectMetaData    = ujson.read(os.read(os.resource(getClass.getClassLoader) / "reportMetaData.json"))
      val updatedProjectMetaData = projectMetaData

      val reportMetaDataJson = ujson.write(updatedProjectMetaData)
      val reportDataJs       = os.read(os.resource(getClass.getClassLoader) / "assets" / "data.js")
      val updatedReportJs    = reportDataJs
        .replaceAllLiterally("'%INJECT_AXE_VIOLATIONS%'", axeResults)
        .replaceAllLiterally("'%INJECT_REPORT_METADATA%'", reportMetaDataJson)
      os.write.over(os.Path(reportDirectory.value / "html-report" / "assets" / "data.js"), updatedReportJs)

      // Write the updated HTML to the output file
      os.write.over(
        os.Path(reportDirectory.value / "html-report" / htmlReport),
        os.read(os.resource(getClass.getClassLoader) / htmlReport)
      )
    } else {
      log.error("No axe results found to generate accessibility assessment report.")
    }
  }
}

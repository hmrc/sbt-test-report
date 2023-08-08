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
import com.typesafe.config.ConfigFactory
import scala.jdk.CollectionConverters.*

object TestReportPlugin extends AutoPlugin {

  object autoImport {
    val testReport      = taskKey[Unit]("generate test report")
    val outputDirectory = settingKey[File]("output directory")
    val outputFile      = settingKey[File]("output file")
  }

  import autoImport.*

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    outputDirectory := (Keys.target.value / "test-reports" / "accessibility-assessment"),
    outputFile := (outputDirectory.value / "html-report" / "index.html")
  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val log = sbt.Keys.streams.value.log
    log.info("Generating accessibility assessment report")

    os.makeDir.all(os.Path(outputDirectory.value / "html-report" / "assets"))

    val assets = List(
      "enum.1.13.5.min.js",
      "jquery.1.13.5.dataTables.min.css",
      "jquery.1.13.5.dataTables.min.js",
      "jquery.3.7.0.min.js",
      "dataTables.js",
      "reset.css",
      "style.css"
    )

    assets.foreach { fileName =>
      os.write.over(
        os.Path(outputDirectory.value / "html-report" / "assets" / fileName),
        os.read(os.resource(getClass.getClassLoader) / "assets" / fileName)
      )
    }

    val axeResultsDirectory = os.Path(outputDirectory.value / "axe-results")
    val axeResults          = os.list.stream(axeResultsDirectory).filter(os.isDir).map { timestampDirectory =>
      ujson.read(os.read(timestampDirectory / "axeResults.json"))
    }

    val projectName               = Keys.name.value
    val isJenkinsBuild            = sys.env.contains("BUILD_ID")
    val jenkinsBuildId            = sys.env.get("BUILD_ID")
    val jenkinsBuildUrl           = sys.env.getOrElse("BUILD_URL", "#")
    val axeResultsVersion         = axeResults.take(1).map(firstResult => firstResult("testEngine")("version").str)
    val axeResultsTotalCount      = axeResults.count()
    val axeResultsViolationsCount = axeResults.map(result => result("violations").arr.length).sum
    val reportDocumentationUrl    = "https://github.com/hmrc/accessibility-assessment/blob/main/docs/READING-THE-REPORT.md"

    val customFiltersFileName     = "custom-filters.json"
    def hasCustomFilters: Boolean = os.exists(os.pwd / customFiltersFileName)
    case class UrlFilter(url: String, reason: String)
    lazy val customUrlFilters     = ConfigFactory
      .parseFile(new java.io.File(customFiltersFileName))
      .getConfigList("url-filters")
      .asScala
      .map(config =>
        UrlFilter(
          config.getString("url"),
          config.getString("reason")
        )
      )

    def urlIsFiltered(url: String): Boolean = customUrlFilters.exists(_.url == url)

    def checkUrlForFilter(url: String): String = {
      val filtered = customUrlFilters.filter(_.url == url)
      if (filtered.isEmpty) "" else filtered.head.reason
    }

    os.write.over(
      os.Path(outputFile.value),
      "<!DOCTYPE html>" + html(
        head(
          meta(charset := "utf-8"),
          meta(name := "viewport", content := "width=device-width, initial-scale=1"),
          tag("title")(s"HMRC accessibility assessment for $projectName"),
          meta(name := "description", content := s"HMRC accessibility assessment for $projectName"),
          link(rel := "stylesheet", href := "assets/reset.css"),
          link(rel := "stylesheet", href := "assets/jquery.1.13.5.dataTables.min.css"),
          link(rel := "stylesheet", href := "assets/style.css"),
          script(src := "assets/jquery.3.7.0.min.js"),
          script(src := "assets/jquery.1.13.5.dataTables.min.js"),
          script(src := "assets/enum.1.13.5.min.js")
        ),
        body(
          header(
            cls := "flow region wrapper",
            h1("HMRC accessibility assessment for ", projectName),
            p(
              "Get some help from our latest ",
              a(href := reportDocumentationUrl, "report documentation", target := "_blank"),
              "."
            ),
            table(
              cls := "summary",
              thead(
                tr(th(colspan := 2, "Summary"))
              ),
              tbody(
                tr(td("Test repository"), td(a(href := s"https://github.com/hmrc/$projectName", projectName))),
                tr(
                  td("Build number"),
                  if (isJenkinsBuild) td(a(href := jenkinsBuildUrl, jenkinsBuildId))
                  else td("N/A")
                ),
                tr(td("Pages assessed"), td(axeResultsTotalCount)),
                tr(td("Axe violations"), td(axeResultsViolationsCount)),
                tr(td("Axe version"), td(axeResultsVersion))
              )
            )
          ),
          div(
            cls := "flow region wrapper",
            h2(s"Axe violations ($axeResultsViolationsCount)"),
            hr,
            div(
              table(
                id := "axe-violations",
                cls := "display",
                width := "100%",
                thead(
                  tr(
                    th("URL ", a(href := s"$reportDocumentationUrl#url-path", "(?)", target := "_blank")),
                    th("ID ", a(href := s"$reportDocumentationUrl#code-axe-docs", "(?)", target := "_blank")),
                    th("Description ", a(href := s"$reportDocumentationUrl#description", "(?)", target := "_blank")),
                    th("HTML ", a(href := s"$reportDocumentationUrl#usnippet", "(?)", target := "_blank")),
                    th("Impact ", a(href := s"$reportDocumentationUrl#severity", "(?)", target := "_blank")),
                    th(
                      "Further information ",
                      a(href := s"$reportDocumentationUrl#further-information", "(?)", target := "_blank")
                    )
                  )
                ),
                tbody(
                  axeResults.map { result =>
                    val violations = result("violations").arr
                    violations.map { violation =>
                      tr(
                        if (hasCustomFilters && urlIsFiltered(result("url").str))
                          cls := "filtered-violation"
                        else cls := "",
                        td(result("url").str),
                        td(a(href := violation("helpUrl").str, violation("id").str, target := "_blank")),
                        td(violation("description").str),
                        td(
                          pre(
                            violation("nodes").arr.map(instance => pre(whiteSpace := "pre-wrap", instance("html").str))
                          )
                        ),
                        td(data.impact := violation("impact").str, violation("impact").str),
                        if (hasCustomFilters)
                          td(checkUrlForFilter(result("url").str))
                        else td("")
                      )
                    }
                  }
                )
              )
            )
          ),
          script(src := "assets/dataTables.js")
        )
      )
    )
  }
}

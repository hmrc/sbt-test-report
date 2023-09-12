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

import java.text.SimpleDateFormat
import java.util.Calendar

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

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val log                 = sbt.Keys.streams.value.log
    val axeResultsDirectory = os.Path(reportDirectory.value / "axe-results")

    def hasAxeResults: Boolean = os.exists(axeResultsDirectory)

    if (hasAxeResults) {
      log.info("Generating accessibility assessment report ...")
      os.makeDir.all(os.Path(reportDirectory.value / "html-report" / "assets"))

      val assets = List(
        "list.min.js",
        "style.css",
        "report.js",
        "data.js"
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
          ujson.write(ujsonValue)
        }
        .mkString(",")

      val jenkinsBuildId = sys.env.get("BUILD_ID")
      val jenkinsBuildUrl = sys.env.getOrElse("BUILD_URL", "#")

      val format = new SimpleDateFormat("dd M yyyy")
      val date = format.format(Calendar.getInstance().getTime())
      val reportMetaData = ReportMetaData(
        Keys.name.value,
        jenkinsBuildId,
        jenkinsBuildUrl,
        date
      )

//      val projectMetaData        = ujson.read(os.read(os.resource(getClass.getClassLoader) / "reportMetaData.json"))
//      val updatedProjectMetaData = projectMetaData
//      val reportMetaDataJson = ujson.write(updatedProjectMetaData)

      val reportDataJs       = os.read(os.resource(getClass.getClassLoader) / "assets" / "data.js")
      val updatedReportJs    = reportDataJs
        .replaceAllLiterally("'%INJECT_AXE_VIOLATIONS%'", axeResults)
        .replaceAllLiterally("'%INJECT_REPORT_METADATA%'", ReportMetaData.getJsonString(reportMetaData))
      os.write.over(os.Path(reportDirectory.value / "html-report" / "assets" / "data.js"), updatedReportJs)

      os.write.over(
        os.Path(reportDirectory.value / "html-report" / htmlReport),
        os.read(os.resource(getClass.getClassLoader) / htmlReport)
      )
    } else {
      log.error("No axe results found to generate accessibility assessment report.")
    }
  }
}

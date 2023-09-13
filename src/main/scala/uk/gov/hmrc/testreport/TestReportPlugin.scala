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
import _root_.io.circe.syntax.EncoderOps
import uk.gov.hmrc.testreport.ReportMetaData.*

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

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
        "data.js",
        "md5.min.js",
        "report.js",
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
          val ujsonValue = ujson.read(os.read(timestampDirectory / "axe-report.json"))
          ujson.write(ujsonValue)
        }
        .mkString(",")

      val jenkinsBuildId  = sys.env.get("BUILD_ID")
      val jenkinsBuildUrl = sys.env.getOrElse("BUILD_URL", "#")

      def getOrdinalSuffix(day: Int): String =
        if (day % 10 == 1 && day != 11) {
          "st"
        } else if (day % 10 == 2 && day != 12) {
          "nd"
        } else if (day % 10 == 3 && day != 13) {
          "rd"
        } else {
          "th"
        }

      def formatDate(date: Date): String = {
        val dayFormat        = new SimpleDateFormat("d")
        val restOfDateFormat = new SimpleDateFormat("MMMM yyyy 'at' hh:mma")

        val day           = dayFormat.format(date).toInt
        val ordinalSuffix = getOrdinalSuffix(day)

        s"$day$ordinalSuffix ${restOfDateFormat.format(date)}"
      }

      val date               = formatDate(Calendar.getInstance().getTime)
      val reportMetaData     = ReportMetaData(
        Keys.name.value,
        jenkinsBuildId,
        jenkinsBuildUrl,
        date
      )
      val reportMetaDataJson = reportMetaData.asJson;
      val jsonString         = reportMetaDataJson.noSpaces

      val reportDataJs    = os.read(os.resource(getClass.getClassLoader) / "assets" / "data.js")
      val updatedReportJs = reportDataJs
        .replaceAllLiterally("'%INJECT_AXE_VIOLATIONS%'", axeResults)
        .replaceAllLiterally("'%INJECT_REPORT_METADATA%'", jsonString)
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

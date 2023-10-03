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

import _root_.io.circe.syntax.EncoderOps
import sbt.*
import uk.gov.hmrc.testreport.DataFormatter.formatDate
import uk.gov.hmrc.testreport.ReportMetaData.*

import java.nio.file.{FileSystems, Path}
import java.util.{Calendar, Collections}

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
      val targetAssetsPath = reportDirectory.value / "html-report" / "assets"
      os.makeDir.all(os.Path(targetAssetsPath))

      val sourceFolder = os.Path(pluginResourcesAssetsPath())
      val targetFolder = os.Path(targetAssetsPath)
      os.copy.over(sourceFolder, targetFolder, createFolders = true, replaceExisting = true)

      val axeResults = os.list
        .stream(axeResultsDirectory)
        .filter(os.isDir)
        .map { timestampDirectory =>
          val ujsonValue = ujson.read(os.read(timestampDirectory / "axeResults.json"))
          ujson.write(ujsonValue)
        }
        .mkString(",")

      val jenkinsBuildId  = sys.env.get("BUILD_ID")
      val jenkinsBuildUrl = sys.env.getOrElse("BUILD_URL", "#")

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
        .replaceAllLiterally("INJECT_AXE_VIOLATIONS", axeResults)
        .replaceAllLiterally("INJECT_REPORT_METADATA", jsonString)
      os.write.over(os.Path(targetAssetsPath / "data.js"), updatedReportJs)

      val htmlReport = "index.html"
      os.write.over(
        os.Path(reportDirectory.value / "html-report" / htmlReport),
        os.read(os.resource(getClass.getClassLoader) / htmlReport)
      )
    } else {
      log.error("No axe results found to generate accessibility assessment report.")
    }
  }

  private def pluginResourcesAssetsPath() = {
    val uri                = getClass.getClassLoader.getResource("assets").toURI
    val fileSystem         = FileSystems.newFileSystem(uri, Collections.emptyMap(), null)
    val absoluteAssetsPath = fileSystem.getPath("/assets")
    absoluteAssetsPath
  }
}

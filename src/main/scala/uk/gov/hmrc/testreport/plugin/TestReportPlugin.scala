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

package uk.gov.hmrc.testreport.plugin

import sbt.*
import uk.gov.hmrc.testreport.model.Violation.GroupedViolations
import uk.gov.hmrc.testreport.model.{AxeViolation, BuildDetails, ExclusionRule}
import uk.gov.hmrc.testreport.plugin.ExclusionRuleReader.partitionViolations
import uk.gov.hmrc.testreport.report.AccessibilityReport.htmlReport

import scala.Console.{GREEN, RED, RESET, YELLOW}

object TestReportPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val testReport          = taskKey[Unit]("generate test report")
    val testReportDirectory = settingKey[File]("test report directory")
    val a11yExclusionRules  = settingKey[File]("a11y exclusion rules file")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    testReportDirectory := Keys.target.value / "test-reports",
    a11yExclusionRules := Keys.baseDirectory.value / "accessibility-assessment.json"
  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val axeResultsDirectory    = os.Path(testReportDirectory.value / "accessibility-assessment" / "axe-results")
    val a11yExclusionRulesFile = os.Path(a11yExclusionRules.value)
    val logger                 = sbt.Keys.streams.value.log

    if (os.exists(axeResultsDirectory)) {
      logger.info("Analysing accessibility assessment results ...")

      val htmlReportDirectory: File = testReportDirectory.value / "accessibility-assessment" / "html-report"
      val buildDetails              = BuildDetails(
        projectName = Keys.name.value,
        jenkinsBuildId = sys.env.getOrElse("BUILD_ID", "BUILD_ID"),
        browser = sys.props.getOrElse("browser", "BROWSER").capitalize,
        isJenkinsBuild = sys.env.contains("BUILD_ID"),
        jenkinsBuildUrl = sys.env.getOrElse("BUILD_URL", "BUILD_URL"),
        htmlReportFilename = (htmlReportDirectory / "index.html").toString
      )

      // Get filter rules from json file in test repo
      val a11yExclusions: List[ExclusionRule] = if (os.exists(a11yExclusionRulesFile)) {
        ujson
          .read(os.read.stream(a11yExclusionRulesFile))("exclusions")
          .arr
          .map(rule => ExclusionRule(rule("path").str, rule("reason").str))
          .toList
      } else {
        List.empty[ExclusionRule]
      }

      def friendlyArmadillo: String =
        s" ________________________________" +
          "\n/ Well howdy, partner! Just a   " +
          "\\\n| heads up, I reckon you'll need |" +
          "\n| to mosey on over to your       |" +
          "\n| settings and configure those   |" +
          "\n| exclusions correctly for the   |" +
          "\n| accessibility report. It's     |" +
          "\n| crucial for us to navigate     |" +
          "\n\\ through everything smoothly!   /" +
          "\n --------------------------------" +
          "\n         \\\n          " +
          "\\\n               ,.-----__" +
          "\n            ,:::://///,:::-." +
          "\n           /:''/////// ``:::`;/|/" +
          "\n          /'   ||||||     :://'`" +
          "\\\n        .' ,   ||||||     `/(A11Y" +
          "\\\n  -===~__-'\\__X_`````\\_____/~`-._ `." +
          s"\n              ~~        ~~       `~-'"

      def errorTitle(): Unit = {
        logger.error("________________________________")
        logger.error("MISCONFIGURED EXCLUSION/S")
        logger.error("--------------------------------")
      }

      if (a11yExclusions.exists(_.reason.isEmpty) || a11yExclusions.exists(_.path.isEmpty)) {
        logger.error(friendlyArmadillo)
        errorTitle()
        a11yExclusions
          .filterNot(rule => rule.reason.nonEmpty && rule.path.nonEmpty)
          .foreach(rule => logger.error(s"$rule"))
        logger.error("________________________________")
      } else {
        // Get all axe violations
        val rawAxeViolations: List[AxeViolation] = (for {
          reportDir <- os.list.stream(axeResultsDirectory).filter(os.isDir)
          reportJson = ujson.read(os.read(reportDir / "axeResults.json"))
          violation <- reportJson("violations").arr
          snippet   <- violation("nodes").arr
        } yield AxeViolation(
          reportJson("url").str,
          violation("help").str,
          violation("helpUrl").str,
          violation("impact").str,
          snippet("html").str
        )).toList

        // partition into violations + excluded violations
        val (excludedAxeViolations, includedAxeViolations) = partitionViolations(rawAxeViolations, a11yExclusions)

        // Write HTML document
        logger.info("Writing accessibility assessment report ...")

        // Copy styles
        os.makeDir.all(os.Path(htmlReportDirectory / "css"))
        os.write.over(
          os.Path(htmlReportDirectory / "css" / "report.css"),
          os.read(os.resource(getClass.getClassLoader) / "assets" / "styles" / "report.css")
        )

        val includedViolations = includedAxeViolations.group.sorted
        val excludedViolations = excludedAxeViolations.group.sorted
        os.write.over(
          os.Path(buildDetails.htmlReportFilename),
          htmlReport(buildDetails, includedViolations, excludedViolations)
        )

        if (includedAxeViolations.nonEmpty) {
          logger.error(s"${RED}Accessibility assessment: ${includedAxeViolations.length} violations found$RESET")
        } else {
          logger.info(s"${GREEN}Accessibility assessment: ${includedAxeViolations.length} violations found$RESET")
        }

        if (excludedAxeViolations.nonEmpty) {
          logger.warn(
            s"$YELLOW                         : filtered out ${excludedAxeViolations.length} violations$RESET"
          )
        }

        // Write axe violations count file
        val axeViolationsCountFile = axeResultsDirectory / "axeViolationsCount.json"
        os.write.over(axeViolationsCountFile, includedViolations.length.toString)

        if (buildDetails.isJenkinsBuild) {
          logger.info(
            s"Wrote accessibility assessment report to ${buildDetails.jenkinsBuildUrl}Accessibility_20Assessment_20Report/ "
          )
        } else {
          logger.success(
            s"Wrote accessibility assessment report to file://${buildDetails.htmlReportFilename}"
          )
        }
      }
    } else {
      logger.error("No accessibility assessment results to analyse.")
    }
  }

}

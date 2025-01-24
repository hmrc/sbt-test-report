import scala.sys.process.Process
import play.api.libs.json._

lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),

    TaskKey[Unit]("check") := {
      def stripAnsiColourCodes(str: String): String =
        str.replaceAll("\u001B\\[[;\\d]*m", "")

      val process = Process("sbt testReport")
      val out     = stripAnsiColourCodes(process !!)

      val expectedOutput = "[info] Accessibility assessment: 0 violations found\n" +
        "[warn] Accessibility assessment: filtered out 1 violations"

      if (!out.contains(expectedOutput)) {
        val reportHtmlProcess = Process("cat target/test-reports/accessibility-assessment/html-report/index.html")
        val reportHtml        = reportHtmlProcess !!

        sys.error("Unexpected output:\n" + out + "\nReport HTML:\n" + reportHtml)
      }

      val violationsCountProcess =
        Process("cat target/test-reports/accessibility-assessment/axe-results/axeViolationsCount.json")
      val violationsCountJson = violationsCountProcess !!

      val json = Json.parse(violationsCountJson)
      val violationsCount = (json \ "violationsCount").as[Int]
      val excludedViolationsCount = (json \ "excludedViolationsCount").as[Int]
      val excludedServiceViolationsCount = (json \ "excludedServiceViolationsCount").as[Int]

      val expectedViolationsCount = 0
      val expectedExcludedViolationsCount = 1
      val expectedExcludedServiceViolationsCount = 2

      // Check values
      if (violationsCount != expectedViolationsCount)
        sys.error(s"Unexpected violations count: $violationsCount")
      if (excludedViolationsCount != expectedExcludedViolationsCount)
        sys.error(s"Unexpected excluded violations count: $excludedViolationsCount")
      if (excludedServiceViolationsCount != expectedExcludedServiceViolationsCount)
        sys.error(s"Unexpected excluded service violations count: $excludedServiceViolationsCount")
      ()
    }
  )

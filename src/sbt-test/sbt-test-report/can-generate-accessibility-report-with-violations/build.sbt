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
      val out = stripAnsiColourCodes(process !!)

      val expectedOutput = "[error] Accessibility assessment: 1 violations found"
      if (!out.contains(expectedOutput)) sys.error("unexpected output:\n" + out)

      val violationsCountProcess =
        Process("cat target/test-reports/accessibility-assessment/axe-results/axeViolationsCount.json")
      val violationsCountJson = violationsCountProcess !!

      val json = Json.parse(violationsCountJson)
      val violationsCount = (json \ "violationsCount").as[String]
      val excludedViolationsCount = (json \ "excludedViolationsCount").as[String]
      val excludedServiceViolationsCount = (json \ "excludedServiceViolationsCount").as[String]

      val expectedViolationsCount = "1"
      val expectedExcludedViolationsCount = "1"
      val expectedExcludedServiceViolationsCount = "1"

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

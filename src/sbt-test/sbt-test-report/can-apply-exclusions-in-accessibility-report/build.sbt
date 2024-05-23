import scala.sys.process.Process

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

      val catProcess        = Process("cat target/test-reports/accessibility-assessment/axe-results/axeViolationsCount.json")
      val catOut            = catProcess !!
      val expectedCatOutput = "0"

      if (!out.contains(expectedOutput))
        sys.error("Unexpected output:\n" + out)

      if (!catOut.trim.equals(expectedCatOutput))
        sys.error("Unexpected violation count: " + catOut)
      ()
    }
  )

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

      if (!out.contains(expectedOutput)) {
        val reportHtmlProcess = Process("cat target/test-reports/accessibility-assessment/html-report/index.html")
        val reportHtml        = reportHtmlProcess !!

        sys.error("Unexpected output:\n" + out + "\nReport HTML:\n" + reportHtml)
      }

      val violationsCountProcess  =
        Process("cat target/test-reports/accessibility-assessment/axe-results/axeViolationsCount.json")
      val violationsCount         = violationsCountProcess !!
      val expectedViolationsCount = "0"

      if (!violationsCount.trim.equals(expectedViolationsCount))
        sys.error("Unexpected violation count: " + violationsCount)
      ()
    }
  )

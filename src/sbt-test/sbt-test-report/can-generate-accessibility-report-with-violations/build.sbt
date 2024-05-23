import scala.sys.process.Process

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
      ()
    }
  )

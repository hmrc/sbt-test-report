import scala.sys.process.Process

lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
    TaskKey[Unit]("check") := {
      val process = Process("sbt testReport")
      val out = (process !!)

      val expectedOutput = "[error] Accessibility assessment: 1 violations found"
      if (!out.contains(expectedOutput)) sys.error("unexpected output: " + out)
      ()
    }
  )

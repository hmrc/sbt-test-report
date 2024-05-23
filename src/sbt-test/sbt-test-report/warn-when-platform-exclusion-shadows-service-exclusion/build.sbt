import scala.sys.process.Process

lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
    TaskKey[Unit]("check") := {
      val process = Process("sbt testReport")
      val out     = process !!

      val expectedOutput =
        "[warn] Service exclusion rule (/auth-login-stub) shadowed by platform exclusion rule - you may be able to remove it"

      if (!out.contains(expectedOutput))
        sys.error("unexpected output:\n" + out)
      ()
    }
  )

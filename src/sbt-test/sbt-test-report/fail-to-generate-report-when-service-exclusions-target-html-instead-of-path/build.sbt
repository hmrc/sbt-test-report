import scala.sys.process.Process

lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
    TaskKey[Unit]("check") := {
      val process = Process("sbt testReport")
      val out     = process !!

      val expected =
        "[error] {" +
          "\n[error]  \"path\"  : \"\", \t\t<------" +
          "\n[error]  \"reason\": \"I want to exclude this, but I won't be able to, because service exclusion rules only support paths\" " +
          "\n[error] }"

      if (!out.contains(expected)) sys.error("unexpected output:\n" + out)
      ()
    }
  )

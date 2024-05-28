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

      val expected =
        "[error] {" +
        "\n[error]  \"path\"  : \"/test-only\", " +
        "\n[error]  \"reason\": \"\" \t\t<------" +
        "\n[error] }" +
        "\n[error] {" +
        "\n[error]  \"path\"  : \"\", \t\t<------" +
        "\n[error]  \"reason\": \"Stubbed auth endpoints do not need accessibility testing as it is used purely for testing purposes.\" " +
        "\n[error] }"

      if (!out.contains(expected)) sys.error("unexpected output:\n" + out)
      ()
    }
  )

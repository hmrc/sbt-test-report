lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "0.21.0",
    libraryDependencies ++= Dependencies.compile,
    sbtPlugin := true,
    isPublicArtefact := true
  )

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "0.25.0",
    libraryDependencies ++= Dependencies.compile ++ Dependencies.test,
    sbtPlugin := true,
    isPublicArtefact := true
  )

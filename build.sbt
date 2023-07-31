lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "0.4.0",
    scalaVersion := "2.12.17",
    libraryDependencies := Dependencies.compile,
    sbtPlugin := true,
    isPublicArtefact := true
  )

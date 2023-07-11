lazy val root = (project in file("."))
  .settings(
    name := "sbt-test-report",
    version := "0.1.0",
    scalaVersion := "2.12.17",
    libraryDependencies := Dependencies.compile,
    sbtPlugin := true,
    isPublicArtefact := true
  )

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "0.7.0",
    scalaVersion := "2.12.17",
    libraryDependencies ++= Dependencies.compile,
    sbtPlugin := true,
    isPublicArtefact := true,
    headerMappings := {
      headerMappings.value.filterNot(_._1 == de.heikoseeberger.sbtheader.FileType("html"))
    }
  )

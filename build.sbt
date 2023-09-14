lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "0.12.0",
    scalaVersion := "2.12.17",
    libraryDependencies ++= Dependencies.compile,
    libraryDependencySchemes += "io.circe" %% "circe-core" % "early-semver",
    sbtPlugin := true,
    isPublicArtefact := true,
    headerMappings := { // temp workaround for sbt-auto-build to not add copyright header to html files
      headerMappings.value.filterNot(_._1 == de.heikoseeberger.sbtheader.FileType("html"))
    }
  )

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-test-report",
    version := "1.4.0", // if bumping major version, need to bump the plugin version in scripted tests to match
    libraryDependencies ++= Dependencies.compile ++ Dependencies.test,
    sbtPlugin := true,
    isPublicArtefact := true
  )
  .settings(
    scriptedLaunchOpts ++= {
      val homeDir = sys.props.get("jenkins.home").orElse(sys.props.get("user.home")).getOrElse("")
      val sbtHome = file(homeDir) / ".sbt"
      Seq(
        "-Xmx1024M",
        "-Dplugin.version=" + version.value,
        s"-Dsbt.override.build.repos=${sys.props.getOrElse("sbt.override.build.repos", "false")}",
        // Global base is overwritten with <tmp scripted>/global and can not be reconfigured
        // s"-Dsbt.global.base=$sbtHome/.sbt",
        // We have to explicitly set all the params that rely on base
        s"-Dsbt.boot.directory=${sbtHome / "boot"}",
        s"-Dsbt.repository.config=${sbtHome / "repositories"}",
        s"-Dsbt.boot.properties=file:///${sbtHome / "sbt.boot.properties"}"
      )
    },
    scriptedBufferLog := false
  )

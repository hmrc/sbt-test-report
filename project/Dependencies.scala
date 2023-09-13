import sbt.ModuleID
import sbt._

object Dependencies {
  val circeVersion = "0.12.0"

  val compile: Seq[ModuleID] = Seq(
    "org.scala-lang" %% "toolkit"       % "0.1.7",
    "io.circe"       %% "circe-core"    % circeVersion,
    "io.circe"       %% "circe-generic" % circeVersion,
    "io.circe"       %% "circe-parser"  % circeVersion
  )
}

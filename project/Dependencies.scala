import sbt.ModuleID
import sbt._

object Dependencies {
  val circeVersion = "0.14.3"

  val compile: Seq[ModuleID] = Seq(
    "com.lihaoyi"    %% "scalatags" % "0.12.0",
    "org.scala-lang" %% "toolkit"   % "0.1.7",
    "io.circe"       %% "circe-core" % circeVersion,
    "io.circe"       %% "circe-generic" % circeVersion,
    "io.circe"       %% "circe-parser" % circeVersion,
  )
}

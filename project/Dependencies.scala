import sbt.ModuleID
import sbt._

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.lihaoyi"    %% "scalatags" % "0.12.0",
    "org.scala-lang" %% "toolkit"   % "0.1.7"
  )

}

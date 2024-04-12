import sbt.*

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.lihaoyi"         %% "scalatags"    % "0.12.0",
    "org.scala-lang"      %% "toolkit"      % "0.1.7",
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    "org.jsoup"      % "jsoup"     % "1.11.3" % Test
  )
}

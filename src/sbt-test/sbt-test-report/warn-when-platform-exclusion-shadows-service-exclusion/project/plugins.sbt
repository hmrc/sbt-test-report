resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0-RC5"
addSbtPlugin("uk.gov.hmrc" % "sbt-test-report" % "1.+")
lazy val commonSettings = Seq(
  Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(compilerPlugin(Dependencies.kindProjector), compilerPlugin(Dependencies.betterMonadicFor))
      case _ => Seq.empty
    }
  },
  scalacOptions := {
    val opts = scalacOptions.value
    val wconf = "-Wconf:any:wv"
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => opts :+ wconf
      case _ => opts
    }
  },
  Test / fork := true,
  resolvers += Resolver.sonatypeRepo("releases"),
)

lazy val noPublishSettings =
  commonSettings ++ Seq(publish := {}, publishArtifact := false, publishTo := None, publish / skip := true)

lazy val publishSettings = commonSettings ++ Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  Test / publishArtifact := false
)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .settings(name := "Trace4Cats Dynamic Extras")
  .aggregate(`dynamic-sampling-http4s`, `dynamic-sampling-http-server`)

lazy val `dynamic-sampling-http4s` = (project in file("modules/dynamic-sampling-http4s"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-dynamic-sampling-http4s",
    libraryDependencies ++= Seq(
      Dependencies.circeGeneric,
      Dependencies.http4sCirce,
      Dependencies.http4sDsl,
      Dependencies.http4sServer,
      Dependencies.trace4catsDynamicSamplingConfig,
      Dependencies.trace4catsKernel,
      Dependencies.trace4catsModel
    ),
  )

lazy val `dynamic-sampling-http-server` = (project in file("modules/dynamic-sampling-http-server"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-dynamic-sampling-http-server",
    libraryDependencies ++= Seq(Dependencies.http4sBlazeServer)
  )
  .dependsOn(`dynamic-sampling-http4s`)

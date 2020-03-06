val http4sVersion = "0.21.1"
val logbackVersion = "1.2.3"
val zioVersion = "1.0.0-RC18-1"
val zioMacrosVersion = "0.6.2"
scalaVersion := "2.13.1"

lazy val sharedSettings = Seq(
  scalaVersion     := "2.13.1",
  version          := "0.1.0-SNAPSHOT",
  organization     := "com.github.oen9",
  organizationName := "oen9",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.1.1",
    "io.circe" %%% "circe-parser" % "0.13.0",
    // waiting for scalajs-1.0.0 support
    // "io.circe" %%% "circe-generic-extras" % "0.13.0",
    // "io.circe" %%% "circe-generic" % "0.13.0",
    // "io.circe" %%% "circe-literal" % "0.13.0",
    // "io.scalaland" %%% "chimney" % "0.4.1",
    // "com.softwaremill.quicklens" %%% "quicklens" % "1.4.12"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Ymacro-annotations"
  )
)

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.8.6",
    "me.shadaj" %%% "slinky-web" % "0.6.4",
    "me.shadaj" %%% "slinky-react-router" % "0.6.4",
    "io.suzaku" %%% "diode" % "1.1.8"
  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "16.13.0",
    "react-dom" -> "16.13.0",
    "react-popper" -> "1.3.7",
    "react-router-dom" -> "5.1.2",
    "path-to-regexp" -> "6.1.0",
    "bootstrap" -> "4.4.1",
    "jquery" -> "3.4.1"
  ),
  scalaJSUseMainModuleInitializer := true,
  version.in(webpack) := "4.41.6",
  webpackBundlingMode := BundlingMode.Application,
  webpackBundlingMode.in(fastOptJS) := BundlingMode.LibraryOnly(),
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zioVersion,
    //"dev.zio" %% "zio-macros-core" % zioMacrosVersion,
    //"dev.zio" %% "zio-macros-test" % zioMacrosVersion,
    "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
    "dev.zio" %% "zio-logging-slf4j" % "0.2.3",

    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.github.pureconfig" %% "pureconfig" % "0.12.2",

    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test,

    // remove after scalajs-1.0.0 support
    "io.circe" %%% "circe-generic-extras" % "0.13.0",
    "io.circe" %%% "circe-generic" % "0.13.0",
    "io.circe" %%% "circe-literal" % "0.13.0",
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  target := baseDirectory.value / ".." / "target",
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
)

lazy val app =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full).in(file("."))
    .settings(sharedSettings)
    .jsSettings(jsSettings)
    .jvmSettings(jvmSettings)

lazy val appJS = app.js
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)

lazy val appJVM = app.jvm
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "oracle/graalvm-ce:19.3.0.2-java11",
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in(appJS, Compile)).value,
    mappings.in(Universal) ++= webpack.in(Compile, fullOptJS).in(appJS, Compile).value.map { f =>
      f.data -> s"assets/${f.data.getName()}"
    },
    mappings.in(Universal) ++= Seq(
      (target in(appJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "bootstrap" / "dist" / "css" / "bootstrap.min.css" -> "assets/bootstrap.min.css"
    ),
    bashScriptExtraDefines += """addJava "-Dassets=${app_home}/../assets""""
  )

disablePlugins(RevolverPlugin)

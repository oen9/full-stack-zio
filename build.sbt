val http4sVersion = "0.21.1"
val logbackVersion = "1.2.3"
val zioVersion = "1.0.0-RC17"
scalaVersion := "2.13.1"

lazy val sharedSettings = Seq(
  scalaVersion     := "2.13.1",
  version          := "0.1.0-SNAPSHOT",
  organization     := "com.github.oen9",
  organizationName := "oen9",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.8.6",
    "org.typelevel" %% "cats-core" % "2.1.1",

    "io.circe" %%% "circe-parser" % "0.13.0",
    // waiting for scalajs-1.0.0 support
    // "io.circe" %%% "circe-generic-extras" % "0.13.0",
    // "io.circe" %%% "circe-generic" % "0.13.0",
    // "io.circe" %%% "circe-literal" % "0.13.0",
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
    "me.shadaj" %%% "slinky-web" % "0.6.4",
    "me.shadaj" %%% "slinky-react-router" % "0.6.4",
    // waiting for scalajs-1.0.0 support
    //"io.suzaku" %%% "diode" % "1.1.7"
  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "16.12.0",
    "react-dom" -> "16.12.0",
    "react-popper" -> "1.3.6",
    "react-router-dom" -> "5.1.2",
    "path-to-regexp" -> "6.0.0",
    "bootstrap" -> "4.3.1",
    "jquery" -> "3.4.1"
  ),
  scalaJSUseMainModuleInitializer := true,
  version.in(webpack) := "4.41.2",
  webpackBundlingMode := BundlingMode.Application,
  webpackBundlingMode.in(fastOptJS) := BundlingMode.LibraryOnly(),
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "2.1.1",
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.github.pureconfig" %% "pureconfig" % "0.12.2",

    // remove after scalajs-1.0.0 support
    "io.circe" %%% "circe-generic-extras" % "0.13.0",
    "io.circe" %%% "circe-generic" % "0.13.0",
    "io.circe" %%% "circe-literal" % "0.13.0",
  ),
  target := baseDirectory.value / ".." / "target"
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

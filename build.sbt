scalaVersion := "2.13.2"

val Ver = new {
  val http4s  = "0.21.3"
  val slinky  = "0.6.5"
  val logback = "1.2.3"
  val zio     = "1.0.0-RC20"
  val circe   = "0.13.0"
  val tapir   = "0.14.2"
  val doobie  = "0.9.0"
}

lazy val sharedSettings = Seq(
  scalaVersion     := "2.13.2",
  version          := "0.1.0-SNAPSHOT",
  organization     := "com.github.oen9",
  organizationName := "oen9",
  libraryDependencies ++= Seq(
    "org.typelevel"              %% "cats-core"             % "2.1.1",
    "io.circe"                   %%% "circe-parser"         % Ver.circe,
    "io.circe"                   %%% "circe-generic-extras" % Ver.circe,
    "io.circe"                   %%% "circe-generic"        % Ver.circe,
    "io.circe"                   %%% "circe-literal"        % Ver.circe,
    "com.softwaremill.quicklens" %%% "quicklens"            % "1.5.0",
    "io.scalaland"               %%% "chimney"              % "0.5.2"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Ymacro-annotations",
    "-Ywarn-unused:imports"
  ),
  semanticdbEnabled := true,
  semanticdbVersion := "4.3.10" // scalafixSemanticdb.revision, // https://github.com/scalacenter/scalafix/issues/1109
)

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "me.shadaj"       %%% "slinky-web"                % Ver.slinky,
    "me.shadaj"       %%% "slinky-react-router"       % Ver.slinky,
    "io.suzaku"       %%% "diode"                     % "1.1.8",
    "com.github.oen9" %%% "slinky-bridge-react-konva" % "0.1.1"
  ),
  npmDependencies in Compile ++= Seq(
    "react"            -> "16.13.1",
    "react-dom"        -> "16.13.1",
    "react-popper"     -> "1.3.7",
    "react-router-dom" -> "5.1.2",
    "path-to-regexp"   -> "6.1.0",
    "bootstrap"        -> "4.4.1",
    "jquery"           -> "3.5.1",
    "konva"            -> "4.2.2",
    "react-konva"      -> "16.13.0-0",
    "use-image"        -> "1.0.5"
  ),
  scalaJSUseMainModuleInitializer := true,
  version.in(webpack) := "4.43.0",
  webpackBundlingMode := BundlingMode.Application,
  webpackBundlingMode.in(fastOptJS) := BundlingMode.LibraryOnly()
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio"                     %% "zio"                      % Ver.zio,
    "dev.zio"                     %% "zio-interop-cats"         % "2.1.3.0-RC15",
    "dev.zio"                     %% "zio-logging-slf4j"        % "0.3.0",

    "org.typelevel"               %% "cats-effect"              % "2.1.3", // needed by zio-interop-cats 2.1.3.0-RC15 (incompatible with 2.1.2)

    "org.http4s"                  %% "http4s-blaze-server"      % Ver.http4s,
    "org.http4s"                  %% "http4s-circe"             % Ver.http4s,
    "org.http4s"                  %% "http4s-dsl"               % Ver.http4s,
    "org.http4s"                  %% "http4s-blaze-client"      % Ver.http4s,
    "com.softwaremill.sttp.tapir" %% "tapir-core"               % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % Ver.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % Ver.tapir,
    "ch.qos.logback"              % "logback-classic"           % Ver.logback,
    "com.github.pureconfig"       %% "pureconfig"               % "0.12.2",

    "org.reactivemongo"           %% "reactivemongo"            % "0.20.3",
    "org.flywaydb"                % "flyway-core"               % "6.3.3",
    "org.postgresql"              % "postgresql"                % "42.2.12",
    "org.tpolecat"                %% "doobie-core"              % Ver.doobie,
    "org.tpolecat"                %% "doobie-h2"                % Ver.doobie,
    "org.tpolecat"                %% "doobie-hikari"            % Ver.doobie,
    "org.reactormonk"             %% "cryptobits"               % "1.3",
    "org.mindrot"                 % "jbcrypt"                   % "0.4",

    "dev.zio"                     %% "zio-test"                 % Ver.zio % Test,
    "dev.zio"                     %% "zio-test-sbt"             % Ver.zio % Test,
    "com.h2database"              % "h2"                        % "1.4.200" % Test
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  target := baseDirectory.value / ".." / "target",
  addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full))
)

lazy val app =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("."))
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
    dockerBaseImage := "oracle/graalvm-ce:20.0.0-java11",
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in (appJS, Compile)).value,
    mappings.in(Universal) ++= webpack.in(Compile, fullOptJS).in(appJS, Compile).value.map { f =>
      f.data -> s"assets/${f.data.getName()}"
    },
    mappings.in(Universal) ++= Seq(
      (target in (appJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "bootstrap" / "dist" / "css" / "bootstrap.min.css" -> "assets/bootstrap.min.css"
    ),
    bashScriptExtraDefines += """addJava "-Dassets=${app_home}/../assets""""
  )

disablePlugins(RevolverPlugin)

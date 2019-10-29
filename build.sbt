name := "technical_notes"

organization := "dpratt747"

version := "0.0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-language:higherKinds", "-language:postfixOps", "-Ypartial-unification", "-deprecation",
  "-explaintypes", "-feature", "-language:higherKinds", "-language:implicitConversions", "-Xlint:infer-any",
  "-Xlint:unsound-match", "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-infer-any", "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits", "-Ywarn-unused:locals", "-Ywarn-unused:params", "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates", "-Ywarn-value-discard"
)

enablePlugins(sbtdocker.DockerPlugin, DockerComposePlugin, FlywayPlugin)

lazy val circeVersion = "0.12.0-RC2"
lazy val scalaTestVersion = "3.0.8"
lazy val doobieVersion = "0.8.2"
lazy val http4sVersion = "0.20.11"
lazy val monocleVersion = "2.0.0"

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
)

lazy val dockerIt = Seq(
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.9" % "test",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.9" % "test"
)

lazy val monocle = Seq(
  "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test"
)

lazy val http4s = Seq(
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl"
) map (_ % http4sVersion)

lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-literal"
) map (_ % circeVersion)

lazy val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.mockito" %% "mockito-scala" % "1.6.2" % Test
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

scalacOptions in Test += "-Dconfig.file=/test/resources/application.conf"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.postgresql" % "postgresql" % "42.2.6",
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.5",
  "com.github.pureconfig" %% "pureconfig" % "0.12.0",
  "org.flywaydb" % "flyway-core" % "6.0.3"
) ++ circe ++ testDependencies ++ doobie ++ dockerIt ++ http4s ++ monocle


dockerImageCreationTask := docker.value

dockerAutoPackageJavaApplication(exposedPorts = Seq(8080))

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest")
)

flywayUrl := "jdbc:postgresql://localhost:5432/technical_notes?user=postgres&password=docker"
flywayUser := "postgres"
flywayPassword := "docker"
flywayLocations += "db/migration"
flywayUrl in Test := "jdbc:postgresql://localhost:5432/technical_notes?user=postgres&password=docker"
flywayUser in Test := "postgres"
flywayPassword in Test := "docker"




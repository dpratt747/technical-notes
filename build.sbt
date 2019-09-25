name := "technical_notes"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

lazy val akkaHttpVersion = "10.1.9"
lazy val akkaStreamVersion = "2.5.23"
lazy val circeVersion = "0.12.0-RC2"
lazy val scalaTestVersion = "3.0.8"
lazy val doobieVersion = "0.8.2"

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
)

lazy val dockerIt = Seq(
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.9" % "test",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.9" % "test"
)

lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-literal"
).map(_ % circeVersion)

lazy val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test
)

//parallelExecution in Test := false

resolvers += Resolver.sonatypeRepo("releases")
scalacOptions in Test += "-Dconfig.file=/test/resources/application.conf"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.postgresql" % "postgresql" % "42.2.6",
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.5",
  "com.github.pureconfig" %% "pureconfig" % "0.12.0",
  "org.flywaydb" % "flyway-core" % "6.0.3"
) ++ circe ++ testDependencies ++ doobie ++ dockerIt

//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)
//enablePlugins(DockerComposePlugin)
enablePlugins(FlywayPlugin)

flywayUrl := "jdbc:postgresql://localhost:5432/technical_notes?user=postgres&password=docker"
flywayUser := "postgres"
flywayPassword := "docker"
flywayLocations += "db/migration"
flywayUrl in Test := "jdbc:postgresql://localhost:5432/technical_notes?user=postgres&password=docker"
flywayUser in Test := "postgres"
flywayPassword in Test := "docker"




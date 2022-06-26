import java.time.LocalDate
name := """pubmed2"""
organization := """com.desolution"""
version := s"${LocalDate.now}"

scalaVersion := "2.13.8"

//trapExit := false
run / fork := true

lazy val jsonVersion = "20210307"
lazy val playJsonVersion = "2.10.0-RC6"
lazy val akkaVersion = "2.6.18"
lazy val jacksonVersion = "2.13.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-encoding", "utf8",
  "-nowarn",
  //"-unchecked",
  //"-language:implicitConversions",
  //"-language:higherKinds",
  //"-language:existentials",
  "-language:postfixOps"
)

assembly / assemblyJarName := s"pubmed-${LocalDate.now}.jar"
assembly / mainClass := Some("com.desolution.pubmed2.Pubmed2")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"

// JSON libraries
libraryDependencies += "org.json" % "json" % jsonVersion
libraryDependencies += "com.typesafe.play" %% "play-json" % playJsonVersion

// Akka
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
)

// jackson
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion
)

// Logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % "2.17.1",
  "org.slf4j" % "slf4j-simple" % "1.7.35"
)
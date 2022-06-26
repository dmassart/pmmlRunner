name := """pmmlRunner"""
version := "1.0"
scalaVersion := "3.1.3"
lazy val pmmlVersion = "1.6.3"
lazy val totoVersion = "1.3.10"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

// pmml
libraryDependencies ++= Seq(
	"org.jpmml" % "pmml-evaluator" % pmmlVersion,
	"jakarta.xml.bind" % "jakarta.xml.bind-api" % "3.0.0",
	"com.sun.xml.bind" % "jaxb-impl" % "3.0.0"
)

// scala-csv
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"

assembly / mainClass := Some( "PMMLRunner" )
assembly / assemblyJarName := "pmmlRunner.jar"

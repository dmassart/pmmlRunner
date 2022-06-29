name := """pmmlRunner"""
version := "1.0"
scalaVersion := "3.1.3"
lazy val totoVersion = "1.3.10"
lazy val pmmlVersion = "1.6.3"
lazy val jaxbVersion = "4.0.0"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "scala-csv" % totoVersion,
	"org.jpmml" % "pmml-evaluator" % pmmlVersion,
	"com.sun.xml.bind" % "jaxb-impl" % jaxbVersion
)

assembly / mainClass := Some( "main" )
assembly / assemblyJarName := "pmmlRunner.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

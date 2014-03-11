import play.Project._

name := """XML versioning Test"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.webjars" %% "webjars-play" % "2.2.0", 
	"org.webjars" % "bootstrap" % "2.3.1",
	"mysql" % "mysql-connector-java" % "5.1.27"
	)

libraryDependencies += javaEbean

playJavaSettings

import play.Project._

name := "XML versioning common API"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.webjars" %% "webjars-play" % "2.2.0", 
	"org.webjars" % "bootstrap" % "2.3.1",
	"mysql" % "mysql-connector-java" % "5.1.27",
	"org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.0.201403021825-r",
	"commons-io" % "commons-io" % "2.4",
	"exist" % "exist" % "0.9.2",
	"xmldb" % "xmldb-api" % "20021118",
	"xmlunit" % "xmlunit" % "1.5"
	)

libraryDependencies += javaEbean

resolvers += "Repos Open Repository" at "http://reposserver.sourceforge.net/maven/"

playJavaSettings

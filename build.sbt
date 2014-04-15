import play.Project._

name := "XML versioning common API"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.webjars" %% "webjars-play" % "2.2.0", 
	"org.webjars" % "bootstrap" % "2.3.1",
	"mysql" % "mysql-connector-java" % "5.1.27",
	"org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.0.201403021825-r",
	"commons-io" % "commons-io" % "2.4",
	"org.tmatesoft.svnkit" % "svnkit" % "1.8.3-1",
	"se.simonsoft" % "cms-item" % "2.1-SNAPSHOT",
	"se.simonsoft" % "cms-backend-svnkit" % "0.9-SNAPSHOT", 
	"se.simonsoft" % "cms-testing" % "0.9-SNAPSHOT",
	"xmlunit" % "xmlunit" % "1.5",
    "javax" % "javaee-api" % "7.0",
    "com.github.sirixdb.sirix" % "sirix-core" % "0.1.2-SNAPSHOT",
    "com.github.sirixdb.sirix" % "sirix-example" % "0.1.2-SNAPSHOT",
    "com.github.sirixdb.sirix" % "sirix-xquery" % "0.1.2-SNAPSHOT"  
	)


libraryDependencies += javaEbean

resolvers += "Oracle Maven Repository" at "http://download.oracle.com/maven/"

resolvers += "sirixDb" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Repos Open Repository" at "http://reposserver.sourceforge.net/maven/"




playJavaSettings

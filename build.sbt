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
    "com.github.sirixdb.sirix" % "sirix-xquery" % "0.1.2-SNAPSHOT",
    "com.github.sirixdb.brackit" % "brackit" % "0.1.3-SNAPSHOT",
	"com.sleepycat" % "je" % "5.0.103",
	"org.aspectj" % "aspectjrt" % "1.6.10",
	"org.slf4j" % "slf4j-api" % "${slf4j.version}",
	"org.perfidix" % "perfidix" % "3.6.6",
	"ch.qos.logback" % "logback-classic" % "0.9.24",
	"com.google.inject" % "guice" % "3.0",
	"com.google.code.gson" % "gson" % "2.2.4",
	"com.google.guava" % "guava" % "16.0.1",
	"com.google.guava" % "guava-testlib" % "16.0.1",
	"com.google.code.findbugs" % "jsr305" % "1.3.9"
  )


libraryDependencies += javaEbean

resolvers += "Oracle Maven Repository" at "http://download.oracle.com/maven/"

resolvers += "sirixDb" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Repos Open Repository" at "http://reposserver.sourceforge.net/maven/"




playJavaSettings

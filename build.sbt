name := "fac-stemi"

organization := "com.lateralthoughts.internal"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

val luceneVersion = "4.7.0"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.xhtmlrenderer"       % "flying-saucer-pdf"           % "9.0.6",
  "net.sf.jtidy"            % "jtidy"                       % "r938",
  "com.google.apis"         % "google-api-services-drive"   % "v2-rev109-1.16.0-rc",
  "com.google.http-client"  % "google-http-client-jackson2" % "1.17.0-rc",
  "com.google.http-client"  % "google-http-client-jackson"  % "1.15.0-rc",
  "net.databinder.dispatch" %% "dispatch-core"              % "0.11.2",
  "org.reactivemongo"       %% "play2-reactivemongo"    %  "0.10.5.akka23-SNAPSHOT",
  "org.apache.lucene"       % "lucene-test-framework"   % luceneVersion % "test",
  "org.apache.lucene"       % "lucene-core"             % luceneVersion,
  "org.apache.lucene"       % "lucene-analyzers-common" % luceneVersion,
  "org.apache.lucene"       % "lucene-queries"          % luceneVersion,
  "org.apache.lucene"       % "lucene-queryparser"      % luceneVersion,
  "org.apache.lucene"       % "lucene-highlighter"      % luceneVersion,
  "org.scalatestplus"       %% "play"                   % "1.1.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(SbtWeb).enablePlugins(PlayScala)
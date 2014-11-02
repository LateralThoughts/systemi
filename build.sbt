name := "fac-stemi"

organization := "com.lateralthoughts.internal"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

val luceneVersion = "4.7.0"

resolvers += Resolver.sonatypeRepo("snapshots")

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
  "org.reactivemongo"       %% "play2-reactivemongo"    %  "0.10.5.akka23-SNAPSHOT",
  "org.apache.lucene"       % "lucene-test-framework"   % luceneVersion % "test",
  "org.apache.lucene"       % "lucene-core"             % luceneVersion,
  "org.apache.lucene"       % "lucene-analyzers-common" % luceneVersion,
  "org.apache.lucene"       % "lucene-queries"          % luceneVersion,
  "org.apache.lucene"       % "lucene-queryparser"      % luceneVersion,
  "org.apache.lucene"       % "lucene-highlighter"      % luceneVersion,
  "com.softwaremill.macwire" %% "macros" % "0.7.1",
  "com.softwaremill.macwire" %% "runtime" % "0.7.1",
  "ws.securesocial"          %% "securesocial" % "master-SNAPSHOT",
  "org.julienrf"            %% "play-json-variants" % "1.0.0",
  "com.jsuereth"            %% "scala-arm" % "1.4",
  "org.scalatestplus"       %% "play"                   % "1.1.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(SbtWeb).enablePlugins(PlayScala)
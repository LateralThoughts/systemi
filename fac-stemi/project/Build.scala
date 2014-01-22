import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

   val appName = "fac-stemi"
   val appVersion = "1.0-SNAPSHOT"

   val appDependencies = Seq(
      "org.xhtmlrenderer" % "flying-saucer-pdf" % "9.0.4",
      "net.sf.jtidy" % "jtidy" % "r938",
      "com.google.apis" % "google-api-services-drive" % "v2-rev109-1.16.0-rc",
      "com.google.http-client" % "google-http-client-jackson2" % "1.17.0-rc",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test"
   )

   val appSettings = Seq(
         // Optional
         // Disable jar for this project (useless)
         publishArtifact in (Compile, packageBin) := false,

         // Disable scaladoc generation for this project (useless)
         publishArtifact in (Compile, packageDoc) := false,

         // Disable source jar for this project (useless)
         publishArtifact in (Compile, packageSrc) := false

         // Where to 'publish'
         //publishTo := Some(Resolver.file("file", file(Path.userHome.absolutePath + "/.ivy2/publish"))),
   )

   val main = play.Project(
      appName,
      appVersion,
      appDependencies,
      settings = Defaults.defaultSettings ++ playScalaSettings ++ appSettings).settings(

   )
}

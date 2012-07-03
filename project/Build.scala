import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "emperor"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.scala-lang" % "scala-compiler"         % "2.9.1",
    "org.specs2"    %% "specs2"                 % "1.8.2"           % "test",
    "mysql"         % "mysql-connector-java"    % "5.1.19",
    "org.mindrot"   % "jbcrypt"                 % "0.3m",
    "org.clapper"   %% "markwrap"               % "0.5.4"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
    scalaVersion := "2.9.1"
  )
}

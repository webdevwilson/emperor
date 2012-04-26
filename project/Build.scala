import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "emperor"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.specs2"    %% "specs2"                 % "1.8.2"           % "test",
    "org.squeryl"   %% "squeryl"                % "0.9.5-RC1",
    "mysql"         % "mysql-connector-java"    % "5.1.19",
    "org.scalaquery"%% "scalaquery"             % "0.10.0-M1",
    "org.mindrot"   % "jbcrypt"                 % "0.3m"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here      
  )
}

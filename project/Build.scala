import sbt._
import Keys._
import play.Project._
import sbtbuildinfo.Plugin._

object ApplicationBuild extends Build {

  val appName         = "emperor"
  val appVersion      = "0.1.0"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.specs2"    %% "specs2"                 % "1.12.1"           % "test",
    // "postgresql"    % "postgresql"              % "9.2-1002.jdbc4",
    "org.mindrot"   % "jbcrypt"                 % "0.3m",
    "org.pegdown"   % "pegdown"                 % "1.2.1",
    "javax.mail"    % "mail"                    % "1.4.1",
    // "commons-codec" % "commons-codec"           % "1.7",
    "org.apache.commons" % "commons-email"      % "1.2",
    "joda-time"     % "joda-time"               % "2.1",
    // ES thingies
    "org.elasticsearch" % "elasticsearch"       % "0.20.5",
    "com.spatial4j" % "spatial4j"               % "0.3",
    "org.clapper"   %% "grizzled-slf4j"         % "1.0.1"
  )

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

  val main = play.Project(appName, appVersion, appDependencies, settings = Defaults.defaultSettings ++ buildInfoSettings).settings(
    // Add your own project settings here
    scalaVersion := "2.10.0"
  ).settings(
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "emp"
  )
}

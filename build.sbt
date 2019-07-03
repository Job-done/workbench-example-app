// Turn this project into a Scala.js project by importing these settings

/*import sbt.Keys._
import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver*/

enablePlugins(WorkbenchPlugin)

scalaVersion := "2.12.8"

val example = crossProject.settings(
  scalaVersion := scalaVersion.value,
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.4.4",
    "com.lihaoyi" %%% "autowire" % "0.2.6",
    "com.lihaoyi" %%% "scalatags" % "0.7.0"
  )
).jsSettings(
  name := "Client",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.7"
  )
)/*.jvmSettings(
  Revolver.settings:_*
)*/.jvmSettings(
  name := "Server",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http"  % "10.1.8",
    "com.typesafe.akka"     %% "akka-stream"      % "2.5.23",

    "com.typesafe.akka" %% "akka-actor" % "2.5.23",
    "org.webjars" % "bootstrap" % "4.3.1"
  )
)

val exampleJS = example.js
val exampleJVM = example.jvm.settings(
  (resources in Compile) += {
    (fastOptJS in (exampleJS, Compile)).value
    (artifactPath in (exampleJS, Compile, fastOptJS)).value
  }
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project exampleJVM" :: s}
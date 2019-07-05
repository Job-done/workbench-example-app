// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

scalaVersion := "2.12.8"

name := "Example"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom"% "0.9.7",
  "com.lihaoyi"  %%% "scalatags"  % "0.7.0",
  "com.lihaoyi"  %%% "scalarx"    % "0.4.0"
)
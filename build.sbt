enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Example"

version := "0.2-SNAPSHOT"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %%% "scalatags" % "0.7.0"
)
enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.13.0"
scalacOptions ++= Seq("-unchecked", /*"-feature",*/ "-deprecation")
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.7"/*,
  "org.scala-lang.modules" %% "scala-async" % "0.10.0",
  "com.nativelibs4java" %% "scalaxy-streams" % "0.3.4" % "provided"*/
)
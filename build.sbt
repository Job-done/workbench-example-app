enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.12"
scalacOptions ++= Seq("-unchecked", /*"-feature",*/ "-deprecation")
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.7",
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  "com.nativelibs4java" %% "scalaxy-loops" % "0.3.4" % "provided"
 //"com.nativelibs4java" %% "scalaxy-streams" % "0.3.4" % "provided"
)

/*
resolvers += Resolver.sonatypeRepo("snapshots")

bootSnippet := "example.ScalaJSExample().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fullOptJS in Compile)
*/

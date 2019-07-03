enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "0.9.7")

// bootSnippet := "example.ScalaJSExample().main(document.getElementById('canvas'));"

// updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)


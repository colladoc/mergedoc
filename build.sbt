name := "Mergedoc"

version := "1.0"

organization := "scala.tools.mergedoc"

scalaVersion := "2.8.0"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

libraryDependencies ++= Seq(
  "commons-lang" % "commons-lang" % "2.5" % "compile",
  "jline" % "jline" % "0.9.94" % "compile"
 )

resolvers += ScalaToolsSnapshots

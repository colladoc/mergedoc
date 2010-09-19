import sbt._

class MergedocProject(info: ProjectInfo) extends DefaultProject(info) {

  override def libraryDependencies = Set(
    "commons-lang" % "commons-lang" % "2.5" % "compile",
    "jline" % "jline" % "0.9.94" % "compile"
  ) ++ super.libraryDependencies

}

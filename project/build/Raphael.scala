import sbt._

class RaphaelProject(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
  override def shouldCheckOutputDirectories = false

  override def compileOptions = super.compileOptions ++ Seq(Optimize, target(Target.Java1_5))
  override def packageOptions = super.packageOptions ++ Seq(MainClass("scan.apps.raphael.App"))

  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"
  val scalaActors = "org.scala-lang" % "scala-actors" % "2.4.0-RC2"
  val h2Database = "com.h2database" % "h2" % "1.3.148"
  val squeryl = "org.squeryl" % "squeryl_2.8.1" % "0.9.4-RC3"
}
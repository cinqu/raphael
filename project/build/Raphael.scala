import sbt._

class RaphaelProject(info: ProjectInfo) extends DefaultProject(info) {
  override def shouldCheckOutputDirectories = false

  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"
  val scalaActors = "org.scala-lang" % "scala-actors" % "2.4.0-RC2"
  val h2Database = "com.h2database" % "h2" % "1.3.148"
  val squeryl = "org.squeryl" % "squeryl_2.8.1" % "0.9.4-RC3"
}
package scan.apps.raphael

import data.{Library}
import scala.xml.{NodeSeq, XML}
import java.io.{File}
import java.lang.System.{getProperty}
import org.squeryl.PrimitiveTypeMode._

object Config {
  private def getConfigDir = {
    val dir = getProperty("user.home") + "/.raphael"
    val file = new File(dir)
    if (!file.exists) {
      file.mkdir
      Library.activate
      inTransaction(Library.create)
    }
    dir
  }

  lazy val configDir = getConfigDir
}
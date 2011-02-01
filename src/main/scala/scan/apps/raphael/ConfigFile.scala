package scan.apps.raphael

import scala.xml.{NodeSeq, XML}
import java.io.{File}
import java.lang.System.{getProperty}

object Config {
  private def getConfigDir = {
    val dir = getProperty("user.home") + "/.raphael"
    val file = new File(dir)
    if(!file.exists) file.mkdir
    dir
  }

  lazy val configDir = getConfigDir
}
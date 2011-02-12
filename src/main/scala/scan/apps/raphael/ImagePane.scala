package scan.apps.raphael

import scala.actors._
import Actor._
import scala.swing._
import data._

class ImagePane extends Panel {
  var imageList: Seq[ImageFile] = Seq.empty
  var index: Int = 0

  def current = {
    if (index < 0)
      index = 0
    else if (index >= imageList.length)
      index = imageList.length - 1

    if (imageList.nonEmpty) imageList(index) else null
  }

  player.start

  private trait Event

  private case object Play extends Event

  private case object Pause extends Event

  private case object Shuffle extends Event

  private case object NoShuffle extends Event

  private object player extends Actor {
    var paused = true
    var shuffle = false

    def act = {
      loop {
        react {
          case Play => {
            paused = false
            next.restart
          }
          case Pause => {
            paused = true
          }
          case Shuffle => {
            shuffle = true
          }
          case NoShuffle => {
            shuffle = false
          }
        }
      }
    }

    val next = actor {
      while (!paused) {
        if (shuffle) {
          import scala.util.Random._
          index = nextInt(imageList.length)
        }
        else {
          if (index < imageList.length - 1) index += 1 else index = 0
        }
        repaint
        Thread.sleep(5000)
      }
    }
  }

  override def paint(g: Graphics2D) = {
    import java.awt.geom.AffineTransform._
    import java.awt.RenderingHints._

    g.clearRect(0, 0, size.width, size.height)

    def getScale(w: Int, h: Int): Double = {
      import scala.math.min

      if (w > this.size.width || h > this.size.height) {
        min(w.toDouble / this.size.width.toDouble, h.toDouble / this.size.height.toDouble)
      }
      else if (w < this.size.width || h < this.size.height) {
        min(this.size.width.toDouble / w.toDouble, this.size.height.toDouble / h.toDouble)
      } else {
        1
      }
    }

    if (current != null) {
      val img = current.image

      g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);

      val scale = getScale(img.getWidth(this.peer), img.getHeight(this.peer))
      val at = getTranslateInstance((this.size.width - scale * img.getWidth(this.peer)) / 2, (this.size.height - scale * img.getHeight(this.peer)) / 2)
      at.scale(scale, scale)
      g.drawImage(img, at, this.peer)
    }
  }

  def first = {
    index = 0
    repaint
  }

  def last = {
    index = imageList.length - 1
    repaint
  }

  def next = {
    index = if (index < imageList.length - 1) index + 1 else 0
    repaint
  }

  def prev = {
    index = if (index > 0) index - 1 else imageList.length - 1
    repaint
  }

  def play = {
    player ! Play
  }

  def pause = {
    player ! Pause
  }

  def shuffle = {
    player ! Shuffle
  }

  def noShuffle = {
    player ! NoShuffle
  }
}
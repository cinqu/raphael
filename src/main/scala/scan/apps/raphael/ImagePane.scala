package scan.apps.raphael

import scala.actors._
import Actor._
import scala.swing._
import org.squeryl.PrimitiveTypeMode._
import data._

class ImagePane(fieldIndex: TextField, lengthLabel: Label, tagList: ListView[Tag]) extends BorderPanel {
  lazy val tagLabel = new Label

  var imageList: Seq[ImageFile] = Seq.empty
  var index: Int = 0

  private def updateField = {
    fieldIndex.text = (index + 1).toString
    lengthLabel.text = "/ " + imageList.length.toString
  }

  private def updateTags = actor {
    tagList.listData = inTransaction {
      current.tags.toSeq
    }
    tagLabel.text = inTransaction {
      current.tags.foldLeft("")(_ + " " + _.name)
    }
  }

  def update = {
    updateField
    updateTags
    repaint
  }

  def current = {
    if (index < 0)
      index = 0
    else if (index >= imageList.length)
      index = imageList.length - 1

    updateField

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
        updateField
        updateTags
        repaint
        Thread.sleep(5000)
      }
    }
  }

  lazy val image = new Panel {
    override def paint(g: Graphics2D) = {
      import java.awt.geom.AffineTransform._
      import java.awt.RenderingHints._

      g.clearRect(0, 0, size.width, size.height)

      def getScale(img_w: Int, img_h: Int): Double = {
        import scala.math.{min}

        val (win_w, win_h) = (this.size.width, this.size.height)

        if (img_w > win_w || img_h > win_h) {
          min(win_w.toDouble / img_w.toDouble, win_h.toDouble / img_h.toDouble)
        }
        else 1
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
  }

  import BorderPanel.Position._

  add(image, Center)
  add(tagLabel, South)

  def first = {
    index = 0
    update
  }

  def last = {
    index = imageList.length - 1
    update
  }

  def next = {
    index = if (index < imageList.length - 1) index + 1 else 0
    update
  }

  def prev = {
    index = if (index > 0) index - 1 else imageList.length - 1
    update
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
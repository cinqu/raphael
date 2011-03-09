package scan.apps.raphael

import scala.actors._
import Actor._
import scala.swing._
import event._
import org.squeryl.PrimitiveTypeMode._
import data._
import java.io.{File}

class ImagePane(fieldIndex: TextField, lengthLabel: Label, tagList: ListView[Tag], infoBox: InfoPane) extends BorderPanel {
  lazy val tagLabel = new Label

  private var _imageList: Seq[ImageFile] = Seq.empty
  private var _index: Int = 0
  private var scaled = true;

  def index = _index

  def index_=(i: Int) = {
    if (i < 0)
      _index = 0
    else if (i >= imageList.length)
      _index = imageList.length - 1
    else
      _index = i
    update
  }

  def imageList = _imageList

  def imageList_=(list: Seq[ImageFile]) {
    _imageList = list
    update
  }

  private def updateField = {
    fieldIndex.text = (index + 1).toString
    lengthLabel.text = "/ " + imageList.length.toString
  }

  private def updateTags = actor {
    inTransaction {
      tagList.listData = current.tags.toSeq
      tagLabel.text = current.tags.foldLeft("")(_ + " " + _.name)
    }
  }

  def update = {
    updateField
    updateTags
    repaint
    requestFocus
  }

  val act1 = actor {
    loop {
      val m = current

      if (m != null) {
        val img = new File(current.path)
        infoBox.title = img.getName
        infoBox.directory = img.getParent
        infoBox.fileSize = img.length

        infoBox.width = m.image.getWidth(this.peer)
        infoBox.height = m.image.getHeight(this.peer)

        infoBox.update

        try {
          Thread.sleep(1000)
        } catch {
          case e: Exception => ()
        }
      }
    }
  }

  def current = if (imageList.nonEmpty) imageList(index) else null

  focusable = true

  listenTo(this.keys)
  reactions += {
    case KeyPressed(_, Key.Left, _, _) => prev
    case KeyPressed(_, Key.Right, _, _) => next
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

        if (!scaled) 1
        else {
          val (win_w, win_h) = (this.size.width, this.size.height)

          if (img_w > win_w || img_h > win_h) {
            min(win_w.toDouble / img_w.toDouble, win_h.toDouble / img_h.toDouble)
          }
          else 1
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
  }

  import BorderPanel.Position._

  add(image, Center)
  add(new BorderPanel {
    add(tagLabel, Center)
    add(new ToggleButton("1:1") {
      action = Action("1:1") {
        if (selected) scaled = false
        else scaled = true;
      }
    }, East)
  }, South)

  def first = {
    index = 0
  }

  def last = {
    index = imageList.length - 1
  }

  def next = {
    index = if (index < imageList.length - 1) index + 1 else 0
  }

  def prev = {
    index = if (index > 0) index - 1 else imageList.length - 1
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
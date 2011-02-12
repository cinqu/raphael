package scan.apps.raphael

import scala.swing._
import event._
import data.ImageFile
import scala.actors._
import Actor._

class ImageShow(images: Array[ImageFile]) extends Frame {
  private var index = 0
  private var shuffle = false

  player.start

  trait Event

  case object Play extends Event

  case object Pause extends Event

  object player extends Actor {
    var paused = true

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
        }
      }
    }

    val next = actor {
      while (!paused) {
        if (shuffle) {
          import scala.util.Random._
          index = nextInt(images.length)
        }
        else {
          if (index < images.length - 1) index += 1 else index = 0
        }
        imagePane.repaint
        Thread.sleep(5000)
      }
    }
  }

  private lazy val infoLabel = new Label("0") {
    horizontalTextPosition = Alignment.Center
  }

  private lazy val imagePane = new Panel {
    preferredSize = new Dimension(600, 600)

    override def paint(g: Graphics2D) = {
      import java.awt.geom.AffineTransform._
      import java.awt.RenderingHints._

      g.clearRect(0, 0, size.width, size.height)

      def getScale(w: Int, h: Int): Double = {
        import scala.math.min

        if (w > this.size.width || h > this.size.height) {
          min(w.toDouble / this.size.width.toDouble, h.toDouble / this.size.height.toDouble)
        } else if (w < this.size.width && h < this.size.height) {
          min(this.size.width.toDouble / w.toDouble, this.size.height.toDouble / h.toDouble)
        } else {
          1
        }
      }

      val img = images(index).image

      g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);

      val scale = getScale(img.getWidth(this.peer), img.getHeight(this.peer))
      val at = getTranslateInstance((this.size.width - scale * img.getWidth(this.peer)) / 2, (this.size.height - scale * img.getHeight(this.peer)) / 2)
      at.scale(scale, scale)
      g.drawImage(img, at, this.peer)
    }
  }

  private def next = {
    if (shuffle) {
      import scala.util.Random._
      index = nextInt(images.length)
    }
    else if (index < images.length - 1) {
      index += 1
    }
    imagePane.repaint
  }

  private def prev = {
    if (index > 0) {
      index -= 1
    }
    imagePane.repaint
  }

  private lazy val controlPane = new BoxPanel(Orientation.Horizontal) {
    contents += new Button(Action("First") {
      index = 0
      imagePane.repaint
    })
    contents += new Button(Action("Previous") {
      prev
    })
    contents += infoLabel
    contents += new Button(Action("Next") {
      next
    })
    contents += new Button(Action("Last") {
      index = images.length - 1
      imagePane.repaint
    })
    contents += new ToggleButton {
      action = Action("Play") {
        if (selected) player ! Play
        else player ! Pause
      }
    }
    contents += new ToggleButton("Shuffle") {
      action = Action("Shuffle") {
        if (selected) shuffle = true
        else shuffle = false
      }
    }
  }

  listenTo(imagePane.keys)

  import Key._

  reactions += {
    case KeyReleased(_, Left, _, _) => next
    case KeyReleased(_, Right, _, _) => prev
  }

  contents = new BorderPanel {

    import BorderPanel.Position._

    add(imagePane, Center)
    add(controlPane, South)
  }
}
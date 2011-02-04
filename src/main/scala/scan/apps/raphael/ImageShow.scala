package scan.apps.raphael

import scala.swing._
import event._
import data.{ImageFile}

class ImageShow(images: Array[ImageFile]) extends Frame {
  private var shownImage = images(0)
  private var index = 0

  private lazy val infoLabel = new Label("0") {
    horizontalTextPosition = Alignment.Center
  }

  private lazy val imagePane = new Panel {
    preferredSize = new Dimension(500, 500)

    override def paint(g: Graphics2D) = {

    }
  }

  private lazy val controlPane = new BoxPanel(Orientation.Horizontal) {
    contents += new Button(Action("First") {
      shownImage = images(0)
      index = 0
      imagePane.repaint
    })
    contents += new Button(Action("Previous") {
      if (index > 0) {
        shownImage = images(index - 1)
        index -= 1
      }
      imagePane.repaint
    })
    contents += infoLabel
    contents += new Button(Action("Next") {
      if (index < images.length - 1) {
        shownImage = images(index + 1)
        index += 1
      }
      imagePane.repaint
    })
    contents += new Button(Action("Last") {
      shownImage = images(images.length - 1)
      index = images.length - 1
      imagePane.repaint
    })
    contents += new ToggleButton("Play")
    contents += new ToggleButton("Shuffle")
  }

  contents = new BorderPanel {

    import BorderPanel.Position._

    add(imagePane, Center)
    add(controlPane, South)
  }
}
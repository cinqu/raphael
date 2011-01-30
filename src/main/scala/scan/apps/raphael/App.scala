package scan.apps.raphael

import scala.swing._
import event._
import data._
import org.squeryl.PrimitiveTypeMode._

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Raphael 0.1"
    preferredSize = new Dimension(800, 600)

    menuBar = new MenuBar {
    }

    lazy val commandPane = new BoxPanel(Orientation.Horizontal) {
      contents += new Button("Open Directory")
      contents += new Button("Start Slideshow")
      contents += new TextField
      contents += new Button("Tag!")
    }

    lazy val imagePane = new FlowPanel

    lazy val tagSearch = new TextField {
      listenTo(keys)

      reactions += {
        case KeyReleased(_, Key.Enter, _, _) => inTransaction{
          println("Searching...")
          tagBox.listData = Library.tags.where(t => t.name like text).toSeq
          tagBox.repaint
        }
      }
    }
    lazy val tagBox: ListView[Tag] = new ListView[Tag](Seq.empty) {
      /*      reactions += {
        
      }*/
    }

    lazy val searchPane = new BorderPanel {

      import BorderPanel.Position._

      preferredSize = new Dimension(200, 400)

      add(tagBox, Center)
      add(tagSearch, North)
    }

    contents = new BorderPanel {

      import BorderPanel.Position._

      add(new SplitPane(Orientation.Vertical, searchPane, imagePane), Center)
      add(commandPane, South)
    }

    Library.activate
  }
}
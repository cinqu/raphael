package scan.apps.raphael

import scala.swing._
import event._
import data._
import org.squeryl.PrimitiveTypeMode._

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Raphael 0.1"
    preferredSize = new Dimension(800, 600)

    lazy val openAction: Action = Action("Open Folder") {
      val chooser = new FileChooser {
        multiSelectionEnabled = false
        fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
      }

      chooser.showOpenDialog(commandPane) match {
        case FileChooser.Result.Approve => Library.doImport(chooser.selectedFile)
      }
    }

    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(openAction)
        contents += new Separator
        contents += new MenuItem(Action("Quit") {
          exit
        })
      }
    }

    lazy val commandPane: BoxPanel = new BoxPanel(Orientation.Horizontal) {
      contents += new Button(openAction)
      contents += new Button("Start Slideshow")
      contents += new TextField
      contents += new Button("Tag!")
    }

    lazy val tagSearch = new TextField

    lazy val imagePane:ListView[ImageFile] = new ListView[ImageFile](Seq.empty){
      renderer = new ImageRenderer(new Label)
    }
    lazy val tagBox: ListView[Tag] = new ListView[Tag](Seq.empty) {
      renderer = new TagRenderer(new Label)
    }

    listenTo(tagSearch, tagBox, imagePane)

    reactions += {
      case EditDone(`tagSearch`) => inTransaction{
        println("Searching...")
        tagBox.listData = Library.tags.where(t => t.name like tagSearch.text).toSeq
        tagBox.repaint
      }
    }

    contents = new BorderPanel {

      import BorderPanel.Position._

      add(new SplitPane(Orientation.Vertical, new BorderPanel {

        import BorderPanel.Position._

        preferredSize = new Dimension(200, 400)

        add(tagBox, Center)
        add(tagSearch, North)
      }, imagePane), Center)
      add(commandPane, South)
    }

    Library.activate
  }
}
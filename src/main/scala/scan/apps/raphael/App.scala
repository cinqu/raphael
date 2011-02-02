package scan.apps.raphael

import scala.swing._
import event._
import data._
import org.squeryl.PrimitiveTypeMode._
import scala.actors._
import Actor._

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Raphael 0.1"
    preferredSize = new Dimension(800, 600)

    lazy val openAction: Action = Action("Add Folder to Collection") {
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
      lazy val newTag = new TextField

      contents += new Button(openAction)
      contents += new Button("Start Slideshow")
      contents += newTag
      contents += new Button(Action("Tag!") {
        val tag = Library.findOrAddTag(newTag.text)
        Library.tag(imagePane.selection.items.head, tag)
      })
    }

    lazy val tagSearch = new TextField

    lazy val imagePane: ListView[ImageFile] = new ListView[ImageFile](Seq.empty) {
      renderer = new ImageRenderer(new Label)
    }
    lazy val tagBox: ListView[Tag] = new ListView[Tag](Seq.empty) {
      renderer = new TagRenderer(new Label)
    }

    listenTo(tagSearch, tagBox.selection, imagePane)

    reactions += {
      case EditDone(`tagSearch`) => actor{
        inTransaction{
          val tags = tagSearch.text.split(' ').toList
          tagBox.listData = tags.flatMap(s => Library.tags.where(_.name like s).toSeq)
        }
      }

      case ListSelectionChanged(`tagBox`, _, _) => actor{
        inTransaction{
          val imgs = tagBox.selection.items.head.images.toSeq
          if (!imgs.isEmpty) imagePane.listData = imgs
        }
      }
    }

    contents = new BorderPanel {

      import BorderPanel.Position._

      add(new SplitPane(Orientation.Vertical, new BorderPanel {

        import BorderPanel.Position._

        preferredSize = new Dimension(200, 400)

        add(new ScrollPane(tagBox), Center)
        add(tagSearch, North)
      }, new ScrollPane(imagePane)), Center)
      add(commandPane, South)
    }

    Library.activate
  }
}
package scan.apps.raphael

import scala.swing._
import event._
import data._
import org.squeryl.PrimitiveTypeMode._
import scala.actors._
import Actor._
import java.awt.{Cursor}

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Raphael 0.1"
    preferredSize = new Dimension(800, 600)

    lazy val waitCursor = new Cursor(Cursor.WAIT_CURSOR)
    lazy val defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR)

    lazy val openAction: Action = Action("Add Folder to Collection") {
      val chooser = new FileChooser {
        multiSelectionEnabled = false
        fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
      }

      chooser.showOpenDialog(commandPane) match {
        case FileChooser.Result.Approve => actor{
          cursor = waitCursor
          Library.doImport(chooser.selectedFile)
          cursor = defaultCursor
        }
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
        actor{
          cursor = waitCursor

          newTag.text.split(' ').foreach{
            t =>
              imagePane.selection.items.foreach(Library.tag(_, Library.findOrAddTag(t)))
              imagePane.repaint
          }

          cursor = defaultCursor
        }
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
        cursor = waitCursor
        inTransaction{
          val tags = tagSearch.text.split(' ').toList
          tagBox.listData = tags.flatMap(s => Library.tags.where(_.name like s).toSeq)
        }
        cursor = defaultCursor
      }

      case ListSelectionChanged(`tagBox`, _, _) => actor{
        cursor = waitCursor

        var imgs: Seq[ImageFile] = Seq.empty

        inTransaction{
          tagBox.selection.items.foreach{
            tag =>
              imgs ++= tag.images.toSeq
          }
        }
        imagePane.listData = imgs.groupBy(_.path).map(_._2.head).toSeq

        cursor = defaultCursor
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
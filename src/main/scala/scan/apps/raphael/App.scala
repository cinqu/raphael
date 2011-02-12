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

    def top = this

    lazy val waitCursor = new Cursor(Cursor.WAIT_CURSOR)
    lazy val defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR)

    def filterTags(tagStr: String) = tagStr.split(' ').map(_.toLowerCase).distinct.filter(_.matches("""!?[^\s,!\.]*""")).toSeq

    lazy val openAction: Action = Action("Add Folder to Collection") {
      val chooser = new FileChooser {
        multiSelectionEnabled = false
        fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
      }

      chooser.showOpenDialog(commandPane) match {
        case FileChooser.Result.Approve => actor {
          top.cursor = waitCursor
          val imgs = Library.doImport(chooser.selectedFile)
          imagePane.listData = imgs
          top.cursor = defaultCursor
          Dialog.showMessage(commandPane, "Imported " + imgs.length + " Images", "", Dialog.Message.Info, null)
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
      contents += new Button(Action("Start Slideshow") {
        if (imagePane.listData.nonEmpty) {
          val v = new ImageShow(imagePane.listData.toArray)
          v.open
        }
      })
      contents += newTag
      contents += new Button(Action("Tag!") {
        actor {
          cursor = waitCursor

          filterTags(newTag.text).foreach {
            t =>
              if (t.startsWith("!")) {
                imagePane.selection.items.foreach(Library.untag(_, Library.findOrAddTag(t.substring(1))))
              } else {
                imagePane.selection.items.foreach(Library.tag(_, Library.findOrAddTag(t)))
              }
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

    listenTo(tagSearch.keys, tagBox.selection, imagePane)

    reactions += {
      case KeyPressed(`tagSearch`, Key.Enter, _, _) => actor {
        cursor = waitCursor
        inTransaction {
          val tags = filterTags(tagSearch.text)
          tagBox.listData = tags.flatMap(s => Library.tags.where(_.name like s).toSeq).sorted(TagOrdering)
        }
        cursor = defaultCursor
      }

      case ListSelectionChanged(`tagBox`, _, _) => actor {
        cursor = waitCursor

        imagePane.listData = inTransaction {
          if (tagBox.selection.items.nonEmpty) {
            var imgs = tagBox.selection.items.head.images.toSeq
            if (tagBox.selection.items.nonEmpty) {
              tagBox.selection.items.tail.foreach {
                tag =>
                  imgs = imgs intersect tag.images.toSeq
              }
            }
            imgs.sorted(ImageFileOrdering)
          } else {
            imagePane.listData
          }
        }

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
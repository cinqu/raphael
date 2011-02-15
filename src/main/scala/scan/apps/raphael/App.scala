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
    title = "Raphael 0.2.8"
    preferredSize = new Dimension(800, 600)

    def top = this

    lazy val waitCursor = new Cursor(Cursor.WAIT_CURSOR)
    lazy val defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR)

    lazy val numField = new TextField("1") {
      horizontalAlignment = Alignment.Right
    }
    lazy val lengthLabel = new Label("/ 0") {
      horizontalTextPosition = Alignment.Left
    }

    lazy val newTag = new TextField

    lazy val infoBox = new InfoPane(numField, lengthLabel)

    private def filterTags(tagStr: String) = tagStr.split(' ').map(_.toLowerCase).distinct.filter(_.matches("""!?[^\s,!\.]*""")).toSeq

    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Add Folder to Collection") {
          val chooser = new FileChooser {
            multiSelectionEnabled = false
            fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
          }

          chooser.showOpenDialog(commandPane) match {
            case FileChooser.Result.Approve => actor {
              top.cursor = waitCursor
              imagePane.imageList = Library.doImport(chooser.selectedFile)
              top.cursor = defaultCursor
              Dialog.showMessage(imagePane, "Imported " + imagePane.imageList.length + " Images", "", Dialog.Message.Info, null)
            }
          }
        })
        contents += new MenuItem(Action("Check Collection") {
          top.cursor = waitCursor
          var num = 0
          actor {
            num = Library.checkImages
          }
          top.cursor = defaultCursor
          Dialog.showMessage(imagePane, "Removed " + num + " images from database.", "", Dialog.Message.Info, null)
        })
        contents += new Separator
        contents += new MenuItem(Action("Quit") {
          exit
        })
      }
    }

    lazy val commandPane: BoxPanel = new BoxPanel(Orientation.Horizontal) {
      contents += new Button(Action("Prev") {
        imagePane.prev
      })
      contents += new Button(Action("Next") {
        imagePane.next
      })

      contents += new ToggleButton {
        action = Action("Play") {
          if (selected) imagePane.play
          else imagePane.pause
        }
      }
      contents += new ToggleButton("Shuffle") {
        action = Action("Shuffle") {
          if (selected) imagePane.shuffle
          else imagePane.noShuffle
        }
      }

      contents += newTag
      contents += new Button(Action("Tag!") {
        tag
      })
    }

    lazy val tagSearch = new TextField
    lazy val tagBox: ListView[Tag] = new ListView[Tag](Seq.empty) {
      renderer = new TagRenderer(new Label)
    }
    lazy val imagePane = new ImagePane(numField, lengthLabel, tagBox, infoBox)

    listenTo(tagSearch.keys, tagBox.selection, numField, newTag.keys)

    reactions += {
      case KeyPressed(`tagSearch`, Key.Enter, _, _) => actor {
        top.cursor = waitCursor
        inTransaction {
          val tags = filterTags(tagSearch.text).flatMap(s => Library.tags.where(_.name like s).toSeq).sorted(TagOrdering)
          imagePane.imageList = if (tags.nonEmpty) {
            var list = tags.head.images.toSeq
            if (tags.length > 1) {
              tags.tail.foreach {
                tag =>
                  list = list intersect tag.images.toSeq
              }
            }
            list.sorted(ImageFileOrdering)
          } else {
            imagePane.imageList
          }
        }
        top.cursor = defaultCursor
      }

      case KeyPressed(`newTag`, Key.Enter, _, _) => tag

      case ListSelectionChanged(`tagBox`, _, _) => actor {
        top.cursor = waitCursor

        imagePane.imageList = inTransaction {
          if (tagBox.selection.items.nonEmpty) {
            var list = tagBox.selection.items.head.images.toSeq
            if (tagBox.selection.items.length > 1) {
              tagBox.selection.items.tail.foreach {
                tag =>
                  list = list intersect tag.images.toSeq
              }
            }
            list.sorted(ImageFileOrdering)
          } else imagePane.imageList
        }
        imagePane.first
        top.cursor = defaultCursor
      }

      case EditDone(`numField`) => {
        imagePane.index = (numField.text.toInt) - 1
      }
    }

    def tag = actor {
      top.cursor = waitCursor

      filterTags(newTag.text).foreach {
        t =>
          if (t.startsWith("!")) {
            Library.untag(imagePane.current, Library.findOrAddTag(t.substring(1)))
          } else {
            Library.tag(imagePane.current, Library.findOrAddTag(t))
          }
          imagePane.update
      }

      top.cursor = defaultCursor
    }

    contents = new BorderPanel {

      import BorderPanel.Position._

      add(new SplitPane(Orientation.Vertical, new BorderPanel {

        import BorderPanel.Position._

        preferredSize = new Dimension(200, 400)

        add(new ScrollPane(tagBox), Center)
        add(tagSearch, North)
        add(infoBox, South)
      }, new ScrollPane(imagePane)), Center)
      add(commandPane, South)
    }

    Library.activate
    tagBox.listData = Seq(Library.untagged)
    imagePane.imageList = inTransaction {
      Library.untagged.images.toSeq
    }
  }
}
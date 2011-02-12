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

    lazy val numField = new TextField("0") {
      horizontalAlignment = Alignment.Right
    }
    lazy val lengthLabel = new Label("/ 0") {
      horizontalTextPosition = Alignment.Center
    }

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
              Dialog.showMessage(commandPane, "Imported " + imagePane.imageList.length + " Images", "", Dialog.Message.Info, null)
            }
          }
        })
        contents += new Separator
        contents += new MenuItem(Action("Quit") {
          exit
        })
      }
    }

    lazy val commandPane: BoxPanel = new BoxPanel(Orientation.Horizontal) {
      lazy val newTag = new TextField

      contents += new Button(Action("First") {
        imagePane.first
        numField.text = imagePane.index.toString
      })
      contents += new Button(Action("Prev") {
        imagePane.prev
        numField.text = imagePane.index.toString
      })
      contents += numField
      contents += lengthLabel
      contents += new Button(Action("Next") {
        imagePane.next
        numField.text = imagePane.index.toString
      })
      contents += new Button(Action("Last") {
        imagePane.last
        numField.text = imagePane.index.toString
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
        actor {
          top.cursor = waitCursor

          filterTags(newTag.text).foreach {
            t =>
              if (t.startsWith("!")) {
                Library.untag(imagePane.current, Library.findOrAddTag(t.substring(1)))
              } else {
                Library.tag(imagePane.current, Library.findOrAddTag(t))
              }
              imagePane.repaint
          }

          top.cursor = defaultCursor
        }
      })
    }

    lazy val tagSearch = new TextField

    lazy val imagePane = new ImagePane
    lazy val tagBox: ListView[Tag] = new ListView[Tag](Seq.empty) {
      renderer = new TagRenderer(new Label)
    }

    listenTo(tagSearch.keys, tagBox.selection, numField)

    reactions += {
      case KeyPressed(`tagSearch`, Key.Enter, _, _) => actor {
        top.cursor = waitCursor
        inTransaction {
          val tags = filterTags(tagSearch.text)
          tagBox.listData = tags.flatMap(s => Library.tags.where(_.name like s).toSeq).sorted(TagOrdering)
        }
        top.cursor = defaultCursor
      }

      case ListSelectionChanged(`tagBox`, _, _) => actor {
        top.cursor = waitCursor

        imagePane.imageList = inTransaction {
          if (tagBox.selection.items.nonEmpty) {
            imagePane.imageList = tagBox.selection.items.head.images.toSeq
            if (tagBox.selection.items.nonEmpty) {
              tagBox.selection.items.tail.foreach {
                tag =>
                  imagePane.imageList = imagePane.imageList intersect tag.images.toSeq
              }
            }
            imagePane.imageList.sorted(ImageFileOrdering)
          } else imagePane.imageList
        }
        imagePane.first
        lengthLabel.text = "/ " + imagePane.imageList.length
        numField.text = imagePane.index.toString
        top.cursor = defaultCursor
      }

      case EditDone(`numField`) => {
        imagePane.index = numField.text.toInt
        imagePane.repaint
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
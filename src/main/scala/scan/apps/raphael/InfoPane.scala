package scan.apps.raphael

import scala.swing._

class InfoPane(numField: TextField, lengthLabel: Label) extends BoxPanel(Orientation.Vertical) {
  var title = ""
  var directory = ""
  var width = 0
  var height = 0
  var fileSize: Long = 0

  def update = {
    titleLabel.text = title
    directoryLabel.text = "in " + directory
    sizeLabel.text = width.toString + " by " + height.toString
    fileSizeLabel.text = (fileSize / 1024).toString + " kb"
  }

  private lazy val titleLabel = new Label {
    horizontalAlignment = Alignment.Center
    horizontalTextPosition = Alignment.Center
    verticalAlignment = Alignment.Center
    verticalTextPosition = Alignment.Center
  }
  private lazy val directoryLabel = new Label {
    horizontalAlignment = Alignment.Center
    horizontalTextPosition = Alignment.Center
    verticalAlignment = Alignment.Center
    verticalTextPosition = Alignment.Center
  }
  private lazy val sizeLabel = new Label {
    horizontalAlignment = Alignment.Center
    horizontalTextPosition = Alignment.Center
    verticalAlignment = Alignment.Center
    verticalTextPosition = Alignment.Center
  }
  private lazy val fileSizeLabel = new Label {
    horizontalAlignment = Alignment.Center
    horizontalTextPosition = Alignment.Center
    verticalAlignment = Alignment.Center
    verticalTextPosition = Alignment.Center
  }

  contents += new BoxPanel(Orientation.Horizontal) {
    contents += numField
    contents += lengthLabel
  }
  contents += titleLabel
  contents += directoryLabel
  contents += sizeLabel
  contents += fileSizeLabel
}
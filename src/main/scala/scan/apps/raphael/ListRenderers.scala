package scan.apps.raphael

import scala.swing._
import event._
import data._
import org.squeryl.PrimitiveTypeMode._
import javax.swing.{ImageIcon}

class TagRenderer(c: Label) extends ListView.AbstractRenderer[Tag, Label](c) {
  def configure(l: ListView[_], selected: Boolean, focused: Boolean, a: Tag, index: Int) = {
    component.xAlignment = Alignment.Left
    component.yAlignment = Alignment.Top
    component.text = a.toString

    if (selected) {
      component.border = Swing.LineBorder(l.selectionBackground, 1)
    } else {
      component.border = Swing.EmptyBorder(1)
    }
  }
}

class ImageRenderer(c: Label) extends ListView.AbstractRenderer[ImageFile, Label](c) {
  def configure(l: ListView[_], selected: Boolean, focused: Boolean, a: ImageFile, index: Int) = {
    component.xAlignment = Alignment.Left
    component.yAlignment = Alignment.Top
    component.icon = new ImageIcon(a.imageSm)
    component.text = inTransaction(a.tags.map(_.name).reduceRight(_ + ", " + _))

    if (selected) {
      component.border = Swing.LineBorder(l.selectionBackground, 1)
    } else {
      component.border = Swing.EmptyBorder(1)
    }
  }
}
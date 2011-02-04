package scan.apps.raphael.data

import org.squeryl._
import dsl._
import PrimitiveTypeMode._

case class ImageFile(val path: String) extends KeyedEntity[Long] {
  var id: Long = 0

  lazy val tags = Library.imageTags.left(this)

  override def toString = path
}

case class Tag(val name: String) extends KeyedEntity[Long] {
  var id: Long = 0

  lazy val images = Library.imageTags.right(this)

  def imageNum = images.count(_ => true)

  override def toString = inTransaction{
    name + " " + imageNum
  }
}

object TagOrdering extends Ordering[Tag] {
  def compare(x: Tag, y: Tag) = x.imageNum - y.imageNum
}

object ImageFileOrdering extends Ordering[ImageFile] {
  def compare(x: ImageFile, y: ImageFile) = scala.math.Ordering.String.compare(x.path, y.path)
}

case class ImageTag(val imageId: Long, val tagId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(imageId, tagId)
}
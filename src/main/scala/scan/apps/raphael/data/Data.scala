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

  override def toString = inTransaction{
    name + " " + images.count(_ => true)
  }
}

case class ImageTag(val imageId: Long, val tagId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(imageId, tagId)
}
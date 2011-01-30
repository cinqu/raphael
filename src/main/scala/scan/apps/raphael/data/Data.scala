package scan.apps.raphael.data

import org.squeryl._
import dsl._
import PrimitiveTypeMode._

case class ImageFile(val id: Long, val path: String) extends KeyedEntity[Long] {
  lazy val tags = Library.imageTags.left(this)

  override def toString = path
}

case class Tag(val id: Long, val name: String) extends KeyedEntity[Long] {
  lazy val images = Library.imageTags.right(this)

  override def toString = inTransaction{
    name + " " + images.count(_ => true)
  }
}

case class ImageTag(val imageId: Long, val tagId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(imageId, tagId)
}
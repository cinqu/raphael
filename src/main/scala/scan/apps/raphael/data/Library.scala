package scan.apps.raphael.data

import org.squeryl.{Schema, SessionFactory, Session}
import org.squeryl.adapters.{H2Adapter}
import org.squeryl.PrimitiveTypeMode._
import scan.apps.raphael.{Config}
import java.io.{File}

object Library extends Schema {
  val images = table[ImageFile]("images")
  val tags = table[Tag]("tags")

  val imageTags = manyToManyRelation(images, tags).via[ImageTag]((i, t, itag) => (itag.imageId === i.id, t.id === itag.tagId))

  lazy val untagged: Tag = inTransaction(findOrAddTag("all"))

  on(images)(i => declare(
    columns(i.id, i.path) are (unique, indexed),
    i.path is (dbType("varchar(255)"))
  ))

  on(tags)(t => declare(
    columns(t.id, t.name) are (unique, indexed),
    t.name is (dbType("varchar(30)"))
  ))

  def activate: Unit = {
    val dbString = "jdbc:h2:" + Config.configDir + "/database"

    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(java.sql.DriverManager.getConnection(dbString, "", ""), new H2Adapter)
    )
  }

  private def listImageFiles(f: File): Array[File] = {
    val image_pattern = (""".*(\.(?i)(jpg|png|gif|bmp))$""").r
    f.listFiles.filter(f => image_pattern.findFirstIn(f.getName).isDefined)
  }

  def doImport(dir: File): Seq[ImageFile] = inTransaction {
    val l = listImageFiles(dir)
    l.toSeq.map(f => findOrAddImage(f.getPath))
  }

  def findOrAddTag(t: String) = inTransaction {
    val r = tags.where(_.name === t)
    if (r.isEmpty) {
      val tmp = Tag(t)
      tags.insert(tmp)
      tmp
    } else {
      r.single
    }
  }

  def findOrAddImage(t: String) = inTransaction {
    val r = images.where(_.path === t)
    if (r.isEmpty) {
      val tmp = ImageFile(t)
      images.insert(tmp)
      imageTags.insert(ImageTag(tmp.id, untagged.id))
      tmp
    } else {
      r.single
    }
  }

  def tag(img: ImageFile, tag: Tag) = inTransaction {
    imageTags.insert(ImageTag(img.id, tag.id))
  }

  def untag(img: ImageFile, tag: Tag) = inTransaction {
    imageTags.deleteWhere(it => (it.imageId === img.id) and (it.tagId === tag.id))
  }

  def checkImages = inTransaction {
    var num = 0
    images.foreach(img => {
      if (!(new File(img.path)).exists) {
        images.deleteWhere(i => i.path === img.path)
        imageTags.deleteWhere(it => it.imageId === img.id)
      }
    })
    num
  }
}
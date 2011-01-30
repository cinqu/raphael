package scan.apps.raphael.data

import org.squeryl.{Schema, SessionFactory, Session}
import org.squeryl.adapters.{H2Adapter}
import org.squeryl.PrimitiveTypeMode._
import scan.apps.raphael.{Config}

object Library extends Schema {
  val images = table[ImageFile]("images")
  val tags = table[Tag]("tags")

  val imageTags = manyToManyRelation(images, tags).via[ImageTag]((i, t, itag) => (itag.imageId === i.id, t.id === itag.tagId))

  on(images)(i => declare(
    columns(i.id, i.path) are (unique, indexed),
    i.path is (dbType("varchar(255)"))
  ))

  on(tags)(t => declare(
    columns(t.id, t.name) are (unique, indexed),
    t.name is (dbType("varchar(30)"))
  ))

  def activate = {
    val dbString = "jdbc:h2:" + Config.configDir + "/database"

    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(java.sql.DriverManager.getConnection(dbString, "", ""), new H2Adapter)
    )

    transaction{
      //create
      Library.printDdl
    }
  }
}
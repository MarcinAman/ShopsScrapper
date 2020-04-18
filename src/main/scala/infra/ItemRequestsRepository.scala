package infra
import slick.jdbc.SQLiteProfile.api._
import xkom.Item

import scala.concurrent.Future

class ItemRequestsRepository {
  lazy val db = Database.forConfig("scraper")

  def setup(): Future[Unit] = {
    db.run(ItemRequestsRow.table.schema.createIfNotExists)
  }

  def usersByRequestedItemId(itemId: String): Future[Seq[String]] = {
    val query = ItemRequestsRow.table.filter(_.id === itemId).map(_.username)
    db.run(query.result)
  }

  def save(item: Item, user: String): Future[Int] = {
    db.run(ItemRequestsRow.table.insertOrUpdate(item.id, item.price, user))
  }
}

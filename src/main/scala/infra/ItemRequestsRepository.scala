package infra
import domain.Item
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future

class ItemRequestsRepository {
  private lazy val db          = Database.forConfig("scraper")
  private val dateTimeProvider = new DateTimeProvider

  def setup(): Future[Unit] = {
    db.run(ItemRequestsRow.table.schema.createIfNotExists)
  }

  def usersByRequestedItemId(itemId: String, price: BigDecimal): Future[Seq[String]] = {
    val query = ItemRequestsRow.table
      .filter(_.id === itemId)
      .filter(e => e.lastNotification < dateTimeProvider.todaysMidnight() || e.lastNotification.isEmpty)
      .filter(_.price <= price)
      .map(_.username)
    db.run(query.result)
  }

  def userWasNotified(item: Item, user: String): Future[Int] = {
    save(item, user, Some(dateTimeProvider.todaysMidnight()))
  }

  def save(item: Item, user: String, lastNotification: Option[Long] = None): Future[Int] = {
    db.run(ItemRequestsRow.table.insertOrUpdate(item.id, item.price, user, lastNotification))
  }
}

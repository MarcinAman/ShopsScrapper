package infra

import slick.jdbc.SQLiteProfile.api._

class ItemRequestsRow(tag: Tag) extends Table[(String, BigDecimal, String, Option[Long])](tag, "items") {
  def id               = column[String]("id")
  def price            = column[BigDecimal]("price")
  def username         = column[String]("username")
  def lastNotification = column[Option[Long]]("last_notification")

  def pk = primaryKey("pk_items", (id, username))

  override def * = (id, price, username, lastNotification)
}

object ItemRequestsRow {
  val table = TableQuery[ItemRequestsRow]
}

package infra

import slick.jdbc.SQLiteProfile.api._

class ItemRequestsRow(tag: Tag) extends Table[(String, BigDecimal, String)](tag, "items") {
  def id       = column[String]("id")
  def price    = column[BigDecimal]("price")
  def username = column[String]("username")

  def pk = primaryKey("pk_items", (id, username))

  override def * = (id, price, username)
}

object ItemRequestsRow {
  val table = TableQuery[ItemRequestsRow]
}

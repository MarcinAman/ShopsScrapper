package xkom

import domain.Item

case class XKomItem(id: String, name: String, price: BigDecimal) extends Item {
  override def shop: String = "x-kom"
}

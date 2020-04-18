package xkom

case class Item(id: String, price: BigDecimal, leftAmount: Option[Int] = None)

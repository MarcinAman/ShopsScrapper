package domain

trait Item {
  def id: String
  def name: String
  def price: BigDecimal
  def shop: String
}

package xkom

object Urls {
  val mainPage                       = "http://www.x-kom.pl"
  def productPage(productId: String) = s"$mainPage/p/$productId"
}

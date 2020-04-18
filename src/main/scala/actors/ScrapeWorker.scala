package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout
import actors.ScrapeWorker.{
  ScrapeCommands,
  ScrapeDailyDealsResponse,
  ScrapeProductDetailsResponse,
  ScrapeWebsiteResponse
}
import xkom.{Item, ProductPage, Scraper}

import scala.concurrent.duration._

object ScrapeWorker {
  sealed trait ScrapeCommands
  final case class ScrapeWebsite(replyTo: ActorRef[ScrapeResponse])                           extends ScrapeCommands
  final case class ScrapeDailyDeals(replyTo: ActorRef[ScrapeResponse])                        extends ScrapeCommands
  final case class ScrapeProductDetails(productId: String, replyTo: ActorRef[ScrapeResponse]) extends ScrapeCommands

  trait ScrapeResponse
  final case class ScrapeWebsiteResponse(item: Item, productPage: ProductPage) extends ScrapeResponse
  final case class ScrapeDailyDealsResponse(item: Item)                        extends ScrapeResponse
  final case class ScrapeProductDetailsResponse(productPage: ProductPage)      extends ScrapeResponse

  def apply(): Behavior[ScrapeCommands] = Behaviors.setup(new ScrapeWorker(_, new Scraper))
}

class ScrapeWorker(context: ActorContext[ScrapeCommands], scraper: Scraper)
    extends AbstractBehavior[ScrapeCommands](context) {
  implicit val timeout: Timeout = 3.seconds

  override def onMessage(msg: ScrapeCommands): Behavior[ScrapeCommands] = {
    msg match {
      case ScrapeWorker.ScrapeWebsite(replyTo) =>
        replyTo ! onFullScrape
        Behaviors.same
      case ScrapeWorker.ScrapeDailyDeals(replyTo) =>
        replyTo ! onDailyDeals
        Behaviors.same
      case ScrapeWorker.ScrapeProductDetails(productId: String, replyTo) =>
        replyTo ! onProductPage(productId)
        Behaviors.same
    }
  }

  private def onFullScrape(): ScrapeWebsiteResponse = {
    val dailyDiscountProduct = scraper.scrapeDailyDiscounts()
    val productPage          = scraper.scrapeProductPage(dailyDiscountProduct.id)
    ScrapeWebsiteResponse(dailyDiscountProduct, productPage)
  }

  private def onDailyDeals(): ScrapeDailyDealsResponse = ScrapeDailyDealsResponse(scraper.scrapeDailyDiscounts())

  private def onProductPage(productId: String): ScrapeProductDetailsResponse =
    ScrapeProductDetailsResponse(scraper.scrapeProductPage(productId))

}

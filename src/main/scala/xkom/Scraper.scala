package xkom

import domain.Item
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.{ElementNode, TextNode}

class Scraper {
  val browser = JsoupBrowser()

  def scrapeDailyDiscounts(): Item = {
    val doc = browser.get(Urls.mainPage)

    val hotShot        = doc >> element("#hotShot")
    val discountsBlock = hotShot >> element(".product-impression")
    val productId      = discountsBlock attr "data-product-id"
    val name           = discountsBlock attr "data-product-name"
    val price          = BigDecimal(discountsBlock attr "data-product-price")

    XKomItem(productId, name, price)
  }

  def scrapeProductPage(productId: String): ProductPage = {
    val doc = browser.get(Urls.productPage(productId))
    ProductPage(categories = categories(doc), parameters = parameters(doc))
  }

  private def parameters(doc: browser.DocumentType) = {
    //Find the div with a specification.
    //We find it by the fact, that it's first child has id=Specyfikacja
    val specDiv = doc >> elementList("div") filter { e =>
      {
        e.childNodes.toSeq.headOption.exists {
          case ElementNode(element) =>
            element.hasAttr("id") && element.attr("id") == "Specyfikacja"
          case TextNode(_) => false
        }
      }
    }

    val flattended = specDiv >> elementList("div") flatten

    val names = flattended.filter(_.hasAttr("width")).map(_.text)
    names
      .foldRight[(Map[String, String], Option[String])]((Map.empty, None)) {
        case (element, (acc, None))       => (acc, Some(element))
        case (element, (acc, Some(prev))) => (acc ++ Map(element -> prev), None)
      }
      ._1
  }

  private def categories(doc: browser.DocumentType) = {
    val categories = doc >> elementList("ul") filter { p =>
      val elements = p >> elementList("li")
      elements.headOption.exists(_.text == "x-kom")
    }

    val moreGeneralCategories = categories >> elementList("a") flatten

    moreGeneralCategories.filter(_.hasAttr("title")).map(_.text)
  }
}

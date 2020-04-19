package slack

import domain.Item
import scalaj.http.{Http, HttpOptions}

class SlackRepository {
  val slackToken = "XD"

  def sendMessageToSlack(item: Item, toUsers: Set[String]) = {
    println(s"sending message to slack for: ${toUsers}")
    toUsers.foreach(user => {
      Http(slackToken)
        .postData(
          s"""{"text":"${message(item, user)}"}"""
        )
        .header("Content-Type", "application/json; charset=utf-8")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString
    })
  }

  private def message(item: Item, user: String) = {
    s"@$user item id=${item.id}, ${item.name} is on sale for ${item.price} in shop ${item.shop}"
  }
}

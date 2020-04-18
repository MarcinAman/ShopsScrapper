package slack

import scalaj.http.{Http, HttpOptions}
import xkom.Item

class SlackRepository {
  val slackToken = "XD"

  def sendMessageToSlack(item: Item, toUsers: Set[String]) = {
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
    s"@$user item ${item.id} is on sale for ${item.price}"
  }
}

package actors

import actors.ScrapeResultProcessor.SendSlackCommandsTo
import actors.ScrapeWorker.ScrapeResponse
import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import infra.ItemRequestsRepository
import slack.SlackRepository
import xkom.Item

import scala.util.{Failure, Success}

object ScrapeResultProcessor {
  sealed trait ScrapeResultCommands
  final case class ProcessWebsiteResponse(websiteResponse: ScrapeResponse) extends ScrapeResultCommands

  case class SendSlackCommandsTo(item: Item, users: Set[String]) extends ScrapeResponse

  def apply(): Behavior[ScrapeResponse] = {
    implicit val ac: ActorSystem = ActorSystem("slack")
    Behaviors.setup[ScrapeResponse](new ScrapeResultProcessor(_).process())
  }
}
class ScrapeResultProcessor(context: ActorContext[ScrapeResponse])(implicit ac: ActorSystem) {
  private val slackRepository        = new SlackRepository
  private val itemRequestsRepository = new ItemRequestsRepository

  private def process(): Behavior[ScrapeResponse] = {
    Behaviors.receiveMessage {
      case ScrapeWorker.ScrapeWebsiteResponse(item, productPage) =>
        context.pipeToSelf(itemRequestsRepository.usersByRequestedItemId(item.id)) {
          case Failure(exception) => throw exception
          case Success(value)     => SendSlackCommandsTo(item, value.toSet)
        }
        Behaviors.same
      case SendSlackCommandsTo(item, users) =>
        slackRepository.sendMessageToSlack(item, users)
        Behaviors.same
    }
  }
}

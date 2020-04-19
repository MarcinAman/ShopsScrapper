package actors

import actors.ScrapeResultProcessor.SendSlackCommandsTo
import actors.ScrapeWorker.ScrapeResponse
import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import domain.Item
import infra.ItemRequestsRepository
import slack.SlackRepository

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object ScrapeResultProcessor {
  sealed trait ScrapeResultCommands
  final case class ProcessWebsiteResponse(websiteResponse: ScrapeResponse) extends ScrapeResultCommands

  case class SendSlackCommandsTo(item: Item, users: Set[String]) extends ScrapeResponse

  def apply(): Behavior[ScrapeResponse] = {
    implicit val ac: ActorSystem      = ActorSystem("slack")
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    Behaviors.setup[ScrapeResponse](new ScrapeResultProcessor(_).process())
  }
}
class ScrapeResultProcessor(context: ActorContext[ScrapeResponse])(
    implicit ac: ActorSystem,
    executionContext: ExecutionContext
) {
  private val slackRepository        = new SlackRepository
  private val itemRequestsRepository = new ItemRequestsRepository

  private def process(): Behavior[ScrapeResponse] = {
    Behaviors.receiveMessage {
      case ScrapeWorker.ScrapeWebsiteResponse(item, productPage) =>
        context.pipeToSelf(itemRequestsRepository.usersByRequestedItemId(item.id, item.price)) {
          case Failure(exception) => throw exception
          case Success(value)     => SendSlackCommandsTo(item, value.toSet)
        }
        Behaviors.same
      case SendSlackCommandsTo(item, users) =>
        //We use blocking api to see how new actors behave
        slackRepository.sendMessageToSlack(item, users)

        val futures = Future.traverse(users)(user => itemRequestsRepository.userWasNotified(item, user))
        Await.result(futures, 30.seconds)
        Behaviors.same
    }
  }
}

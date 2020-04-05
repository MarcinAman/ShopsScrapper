package actors

import actors.ScrapeResultProcessor.{ProcessWebsiteResponse, ScrapeResultCommands}
import actors.ScrapeWorker.ScrapeResponse
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

object ScrapeResultProcessor {
  sealed trait ScrapeResultCommands
  final case class ProcessWebsiteResponse(websiteResponse: ScrapeResponse) extends ScrapeResultCommands

  def apply(): Behavior[ScrapeResultCommands] =
    Behaviors.setup[ScrapeResultCommands](new ScrapeResultProcessor(_).process())
}
class ScrapeResultProcessor(context: ActorContext[ScrapeResultCommands]) {
  private def process(): Behavior[ScrapeResultCommands] = {
    Behaviors.receiveMessage {
      case ProcessWebsiteResponse(websiteResponse) =>
        println(s"processing : $websiteResponse")
        Behaviors.same
    }
  }
}

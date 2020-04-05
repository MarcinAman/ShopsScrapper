package actors

import actors.Scheduler.{SchedulerCommands, StartScheduling, TimerKey, WrappedScrapeResponse}
import actors.ScrapeResultProcessor.ProcessWebsiteResponse
import actors.ScrapeWorker.{ScrapeResponse, ScrapeWebsite, ScrapeWebsiteResponse}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}

import scala.concurrent.duration.FiniteDuration

object Scheduler {
  private case object TimerKey

  sealed trait SchedulerCommands
  case object StartScheduling                                extends SchedulerCommands
  case class WrappedScrapeResponse(response: ScrapeResponse) extends SchedulerCommands

  def apply(after: FiniteDuration): Behavior[SchedulerCommands] = {
    Behaviors
      .supervise[SchedulerCommands] {
        Behaviors.setup[SchedulerCommands] { ctx =>
          Behaviors.withTimers(timers => new Scheduler(ctx, timers, after).idle())
        }
      }
      .onFailure(SupervisorStrategy.restart)
  }
}

class Scheduler(
    context: ActorContext[SchedulerCommands],
    timers: TimerScheduler[SchedulerCommands],
    after: FiniteDuration
) {
  private val scraper =
    context.spawn(
      behavior = Behaviors.supervise(ScrapeWorker()).onFailure(SupervisorStrategy.restart),
      name = "scrape-worker"
    )
  private val processor =
    context.spawn(
      behavior = Behaviors.supervise(ScrapeResultProcessor()).onFailure(SupervisorStrategy.restart),
      name = "scrape-processor"
    )

  val scraperResponseMapper: ActorRef[ScrapeResponse] = context.messageAdapter(WrappedScrapeResponse)

  private def idle(): Behavior[SchedulerCommands] =
    Behaviors.receiveMessage[SchedulerCommands] { _ =>
      timers.startTimerAtFixedRate(TimerKey, StartScheduling, after)
      active
    }

  def active: Behavior[SchedulerCommands] = Behaviors.receiveMessage {
    case Scheduler.StartScheduling =>
      scraper ! ScrapeWebsite(scraperResponseMapper)
      Behaviors.same
    case WrappedScrapeResponse(response) =>
      processor ! ProcessWebsiteResponse(response)
      Behaviors.same
  }
}

package actors

import actors.Scheduler.{SchedulerCommands, StartScheduling, TimerKey}
import actors.ScrapeWorker.ScrapeWebsite
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{Behavior, SupervisorStrategy}

import scala.concurrent.duration.FiniteDuration

object Scheduler {
  private case object TimerKey

  sealed trait SchedulerCommands
  case object StartScheduling extends SchedulerCommands

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

  private def idle(): Behavior[SchedulerCommands] =
    Behaviors.receiveMessage[SchedulerCommands] { _ =>
      timers.startTimerAtFixedRate(TimerKey, StartScheduling, after)
      active
    }

  def active: Behavior[SchedulerCommands] = Behaviors.receiveMessage {
    case Scheduler.StartScheduling =>
      println("Scheduler starts working")
      scraper ! ScrapeWebsite(processor)
      Behaviors.same
  }
}

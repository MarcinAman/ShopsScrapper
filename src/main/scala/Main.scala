import actors.Scheduler
import actors.Scheduler.StartScheduling
import akka.actor.typed.ActorSystem

import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App {

  import akka.actor.typed.scaladsl.AskPattern._
  import akka.util.Timeout

  implicit val timeout: Timeout = 3.seconds

  val system             = ActorSystem(Scheduler(1 seconds), "my-system")
  val rootActor          = system
  implicit val scheduler = schedulerFromActorSystem(system)
  implicit val ec        = system.executionContext

  rootActor.tell(StartScheduling)

  println("Press the any-key to terminate")
  StdIn.readLine()
  system.terminate()
}

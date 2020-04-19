import actors.Scheduler
import actors.Scheduler.StartScheduling
import akka.actor.typed.ActorSystem
import infra.ItemRequestsRepository
import xkom.XKomItem

import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App {

  import akka.actor.typed.scaladsl.AskPattern._
  import akka.util.Timeout

  implicit val timeout: Timeout = 3.seconds

  val system             = ActorSystem(Scheduler(5 seconds), "my-system")
  val rootActor          = system
  implicit val scheduler = schedulerFromActorSystem(system)
  implicit val ec        = system.executionContext

  val requestsRepository = new ItemRequestsRepository

  for {
    _ <- requestsRepository.setup()
    _ <- requestsRepository.save(XKomItem("236497", "Sample item", 2136), "Marcin Aman")
  } yield rootActor.tell(StartScheduling)

  println("Press the any-key to terminate")
  StdIn.readLine()
  system.terminate()
}

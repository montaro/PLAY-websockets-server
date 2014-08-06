package globals

import actors.Hummer
import akka.actor.{ActorRef, Props, ActorSystem}
import play.api._
import scala.concurrent.duration._
import protocol.Run
import scala.concurrent.ExecutionContext.Implicits.global

object King extends GlobalSettings {

  private[this] var _actorSystem: ActorSystem = _
  private[this] var _hummer: ActorRef = _
  var maxIdentifier: Int = 0

  def actorSystem = _actorSystem

  def hummer = _hummer

  override def onStart(app: Application) {
    println("Application has started")
    _actorSystem = ActorSystem("GOT")
    _hummer = actorSystem.actorOf(Props(classOf[Hummer]))
    actorSystem.scheduler.scheduleOnce(10 seconds, hummer, Run)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    _actorSystem.shutdown
    _actorSystem.awaitTermination()
  }

}
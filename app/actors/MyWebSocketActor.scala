package actors

import akka.actor._
import globals.King
import play.api.libs.json.{JsError, JsSuccess, JsObject}
import protocol._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  implicit val duration = 4.seconds
  implicit val timeout = Timeout(duration)

  var identifier: String = _

  King.hummer ! IncrementOpened

  def receive = {
    case msg: JsObject =>
      val res = msg.validate[Action]
      res match {
        case JsSuccess(value, _) =>
          val action: String = value.action
          identifier = value.identifier
          val logMessage = "I received JSON your message: \naction: " + action + "\nidentifier: " + identifier
          log.debug("onMessage:  " + logMessage)
          if ("subscribe" == action) {
            val associateActor = King.actorSystem.actorOf(Props(classOf[AssociateActor], identifier, out), identifier)
            log.info("Subscribe Actor Path:" + associateActor.path.toString)
            try {
              King.maxIdentifier = Math.max(Integer.parseInt(identifier), King.maxIdentifier)
            }
            catch {
              case e: NumberFormatException =>
                log.error("Invalid identifier: " + identifier)
            }
          }
          else if ("unsubscribe" == action) {
            log.info("Got Unsubscribe message for : " + identifier)
            die()
          }
        case JsError(e) =>
          log.error("JSON Error happened: " + e)
      }
  }

  override def postStop() = {
    die()
  }


  def die() {
    King.hummer ! Incrementclosed
    val actorPath = "akka://GOT/user/" + identifier
    val actorSelection = King.actorSystem.actorSelection(actorPath)
    actorSelection.resolveOne() map {
      actorRef =>
        log.info("Closing: " + actorRef.path)
        context.stop(actorRef)
    }
  }
}

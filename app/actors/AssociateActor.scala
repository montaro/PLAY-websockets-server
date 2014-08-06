package actors

import akka.actor.{ActorLogging, Actor, ActorRef}

class AssociateActor(identifier: String, out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      out ! msg
  }

  override def postStop() = {
    log.info("Closing: Websocket: " + identifier)
  }
}
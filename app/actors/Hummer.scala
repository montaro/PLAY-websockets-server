package actors

import java.util.Random
import akka.actor.{ActorLogging, Actor}
import globals.King
import play.api.Logger
import play.api.libs.json.Json
import protocol.{Incrementclosed, IncrementOpened, Run}
import utils.Metrics

class Hummer extends Actor with ActorLogging {

  var opened: Long = 0
  var closed: Long = 0
  private final val random: Random = new Random
  private final val METRICS: Metrics = new Metrics
  private var nextRequestId: Long = 0
  private final val elements: StringBuilder = new StringBuilder

  for (i <- 0 to 500) {
    elements.append(String.valueOf(i))
  }

  def receive = {
    case Run =>
      sendMessageForRandomIdentifier()

    case IncrementOpened =>
      opened = opened + 1

    case Incrementclosed =>
      closed = closed + 1
  }

  private def getRandomIdentifier(): Int = {
    try {
      random.nextInt(King.maxIdentifier) + 1
    }
    catch {
      case e: IllegalArgumentException =>
        0
    }
  }

  def sendMessageForRandomIdentifier(): Unit = {
    try {
      if (opened <= closed) {
        log.info("--------------------------Killing The Hummer!--------------------------")
        context.stop(self)
        return
      }
      val identifier: Int = getRandomIdentifier
      val t: Long = System.currentTimeMillis
      nextRequestId = nextRequestId + 1
      val msg = Json.obj("request" -> nextRequestId,
        "timestamp" -> t,
        "identifier" -> identifier,
        "elements" -> elements.toString)
      context.actorSelection("akka://GOT/user/" + identifier) ! msg.toString
      METRICS.update(msg.toString)
      if (t % 1000 == 0) {
        Logger.debug(METRICS.getSummary.toString)
      }
      self ! Run
    }
    catch {
      case e: Exception => {
        Logger.error("Failed to send message", e)
      }
    }
  }

}

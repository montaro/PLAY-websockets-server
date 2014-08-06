package protocol

import play.api.libs.json._

case class Action(action: String, identifier: String)

object Action {
  implicit val format = Json.format[Action]
}

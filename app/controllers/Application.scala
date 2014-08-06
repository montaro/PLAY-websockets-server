package controllers

import java.util.concurrent.atomic.AtomicLong

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import actors._

object Application extends Controller {

  private final val conns: AtomicLong = new AtomicLong

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def ws = WebSocket.acceptWithActor[JsValue, String] { request => out => {
    Logger.debug("New connection: " + conns.incrementAndGet())
    MyWebSocketActor.props(out)
  }

  }


}
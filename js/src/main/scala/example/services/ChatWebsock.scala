package example.services

import diode.{Effect, NoAction}
import example.shared.Dto
import example.shared.Dto._
import io.circe.generic.extras.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalajs.dom
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.LinkingInfo

object ChatWebsock {
  val protocol = dom.window.location.protocol match {
    case "http:" | "file:" => "ws://"
    case _                 => "wss://"
  }
  val baseUrl = if (LinkingInfo.developmentMode) "localhost:8080" else dom.window.location.host
  val url     = protocol + baseUrl + "/chat"

  def connect(): WebSocket = {
    def onopen(e: Event): Unit = {}

    def onmessage(e: MessageEvent): Unit =
      decode[Dto.ChatDto](e.data.toString)
        .fold(
          err => println(s"error: $err : ${e.data.toString()}"), {
            case u: Dto.ChatUser     => AppCircuit.dispatch(Connected(u))
            case u: Dto.ChatUsers    => AppCircuit.dispatch(InitChatUsers(u))
            case u: Dto.NewChatUser  => AppCircuit.dispatch(AddUser(u))
            case u: Dto.ChatUserLeft => AppCircuit.dispatch(RemoveUser(u))
            case m: Dto.ChatMsg      => AppCircuit.dispatch(AddNewMsg(m))
            case unknown             => println(s"[ws] unsupported data: $unknown")
          }
        )

    def onerror(e: Event): Unit = {
      val msg: String = e
        .asInstanceOf[js.Dynamic]
        .message
        .asInstanceOf[js.UndefOr[String]]
        .fold(s"error occurred!")("error occurred: " + _)
      println(s"[ws] $msg")
    }

    def onclose(e: CloseEvent): Unit =
      AppCircuit.dispatch(Disconnected)

    val ws = new WebSocket(url)
    ws.onopen = onopen _
    ws.onclose = onclose _
    ws.onmessage = onmessage _
    ws.onerror = onerror _
    ws
  }

  def send(ws: dom.WebSocket, data: Dto.ClientMsg): Unit =
    if (ws.readyState == 1) {
      val msg = data.asJson.noSpaces
      ws.send(msg)
    }

  def sendAsEffect(ws: dom.WebSocket, data: Dto.ClientMsg)(implicit ec: ExecutionContext): Effect = Effect.action {
    send(ws, data)
    NoAction
  }
}

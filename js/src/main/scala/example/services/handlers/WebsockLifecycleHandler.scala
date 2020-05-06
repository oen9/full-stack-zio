package example.services.handlers

import cats.implicits._
import com.softwaremill.quicklens._
import diode.ActionHandler
import diode.Effect
import diode.ModelRW
import example.services.ChatConnection
import example.services.ChatWebsock
import example.services.Connect
import example.services.Connected
import example.services.Disconnect
import example.services.Disconnected
import example.services.ReConnect
import example.shared.Dto
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WebsockLifecycleHandler[M](modelRW: ModelRW[M, ChatConnection]) extends ActionHandler(modelRW) {

  override def handle = {
    case Connected(user) =>
      // format: off
      val newValue = value
        .modify(_.user).setTo(user)
        .modify(_.msgs).using(_ :+ Dto.ChatMsg(msg = "connected"))
      // format: on
      updated(newValue)

    case Disconnected =>
      import diode.Implicits.runAfterImpl
      // format: off
      val newValue = value
        .modify(_.users).setTo(Dto.ChatUsers())
        .modify(_.msgs).using(_ :+ Dto.ChatMsg(msg = "disconnected"))
        .modify(_.msgs).using(_ :+ Dto.ChatMsg(msg = "reconnecting in 5 seconds"))
      // format: on
      updated(newValue, Effect.action(ReConnect).after(5.second))

    case ReConnect =>
      value.ws.fold(noChange)(_ => effectOnly(Effect.action(Connect)))

    case Connect =>
      // format: off
      val newValue = value
        .modify(_.ws).setTo(ChatWebsock.connect().some)
        .modify(_.msgs).using(_ :+ Dto.ChatMsg(msg = "connecting ..."))
      // format: on
      updated(newValue)

    case Disconnect =>
      value.ws.fold(()) { ws =>
        ws.onclose = _ => ()
        ws.close()
      }

      // format: off
      val newValue = value
        .modify(_.ws).setTo(none)
        .modify(_.user).setTo(Dto.ChatUser())
        .modify(_.users).setTo(Dto.ChatUsers())
        .modify(_.msgs).using(_ :+ Dto.ChatMsg(msg = "disconnected"))
      // format: on
      updated(newValue)
  }
}

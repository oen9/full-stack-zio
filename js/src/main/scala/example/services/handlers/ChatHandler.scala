package example.services.handlers

import cats.implicits._
import com.softwaremill.quicklens._
import diode.ActionHandler
import diode.Effect
import diode.ModelRW
import example.services.AddNewMsg
import example.services.AddUser
import example.services.ChangeMyChatName
import example.services.ChangeUser
import example.services.ChatConnection
import example.services.ChatWebsock
import example.services.Connect
import example.services.Connected
import example.services.Disconnect
import example.services.Disconnected
import example.services.InitChatUsers
import example.services.RemoveUser
import example.shared.Dto
import example.shared.Dto.ChangeChatName
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

class ChatHandler[M](modelRW: ModelRW[M, ChatConnection]) extends ActionHandler(modelRW) {

  override def handle = {
    case InitChatUsers(users) =>
      val newValue = value.modify(_.users).setTo(users)
      updated(newValue)

    case AddUser(ncu) =>
      val newValue = value.modify(_.users.value).using(_ + ncu.u)
      updated(newValue)

    case RemoveUser(cul) =>
      val newValue = value.modify(_.users.value).using(_ - cul.u)
      updated(newValue)

    case AddNewMsg(msg) =>
      val newValue = value.modify(_.msgs).using(_ :+ msg)
      updated(newValue)

    case ChangeMyChatName(newName) =>
      value.ws.fold(noChange) { ws =>
        val data = ChangeChatName(newName = newName)
        effectOnly(ChatWebsock.sendAsEffect(ws, data))
      }

    case ChangeUser(ChangeChatName(oldUser, newName)) =>
      val userPred: Dto.ChatUser => Boolean = _.some == oldUser
      val newValue = value
        .modify(
          _.users.value.eachWhere(userPred).name
        )
        .setTo(newName)
      updated(newValue)
  }
}

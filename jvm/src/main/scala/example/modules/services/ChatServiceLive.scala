package example.modules.services

import cats.implicits._
import example.model.ChatData.User
import example.modules.services.chatService.ChatService
import example.shared.Dto
import example.shared.Dto.ChangeChatName
import example.shared.Dto.ChatMsg
import example.shared.Dto.ChatUserLeft
import example.shared.Dto.NewChatUser
import example.shared.Dto.UnknownData
import fs2.concurrent.Queue
import io.scalaland.chimney.dsl._
import zio._
import zio.logging.Logger
import zio.logging.LogLevel

class ChatServiceLive(users: Ref[Vector[User]], idCounter: Ref[Int], logger: Logger) extends ChatService.Service {
  def getUsers(): zio.Task[Dto.ChatUsers] =
    for {
      users <- users.get
      dtoUsers = users.map(_.into[Dto.ChatUser].transform)
    } yield Dto.ChatUsers(dtoUsers.toSet)

  def createUser(out: Queue[Task[*], Dto.ChatDto]): Task[Dto.ChatUser] =
    for {
      id <- idCounter.modify(id => (id, id + 1))
      u    = User(id = id, name = "unknown", out = out)
      dtoU = u.into[Dto.ChatUser].transform

      _ <- out.enqueue1(dtoU)
      _ <- handleServerMsg(Dto.NewChatUser(dtoU))

      _        <- users.update(_ :+ u)
      dtoUsers <- getUsers()
      _        <- out.enqueue1(dtoUsers)
    } yield dtoU

  def handleUserMsg(userId: Int, msg: Dto.ClientMsg): Task[Unit] = msg match {
    case chatMsg: ChatMsg =>
      for {
        maybeUser <- users.map(_.find(_.id == userId)).get
        _ <- maybeUser
          .fold(
            logError("ChatMsg", s"userId: $userId not found")
          )(
            broadcast(_, userDto => chatMsg.copy(user = userDto))
          )
      } yield ()

    case cn: ChangeChatName =>
      for {
        maybeOldUser <- users.modify(changeUserName(userId, cn.newName, _))
        _ <- maybeOldUser
          .fold(
            logError("ChangeChatName", s"userId: $userId not found")
          )(
            broadcast(_, userDto => cn.copy(oldUser = userDto))
          )
      } yield ()

    case ud: UnknownData =>
      for {
        users <- users.get
        maybeUser = users.find(_.id == userId)
        _ <- maybeUser.fold(zioUnit)(_.out.enqueue1(ud))
      } yield ()
  }

  def handleServerMsg(msg: Dto.ServerMsg): zio.Task[Unit] = msg match {
    case nu: NewChatUser =>
      broadcast(nu)
    case ul @ ChatUserLeft(u) =>
      for {
        _ <- users.update(_.filter(_.id != u.id))
        _ <- broadcast(ul)
      } yield ()
  }

  def zioUnit: ZIO[Any, Throwable, Unit] = ZIO.unit // Nothing -> Throwable
  def broadcast(msg: Dto.ChatDto): ZIO[Any, Throwable, Unit] =
    for {
      users <- users.get
      _     <- users.foldLeft(zioUnit)((acc, rcvU) => acc *> rcvU.out.enqueue1(msg))
    } yield ()

  def broadcast(u: User, createMsg: Option[Dto.ChatUser] => Dto.ChatDto): ZIO[Any, Throwable, Unit] = {
    val dtoU           = u.into[Dto.ChatUser].transform
    val msgToBroadcast = createMsg(dtoU.some)
    broadcast(msgToBroadcast)
  }

  def changeUserName(userId: Int, newName: String, users: Vector[User]) = {
    import com.softwaremill.quicklens._
    val uIdPred: User => Boolean = _.id == userId

    val maybeOldUser = users.find(uIdPred)
    val updatedUsers = users.modify(_.eachWhere(uIdPred).name).setTo(newName)

    (maybeOldUser, updatedUsers)
  }

  def logError(caller: String, msg: String): ZIO[Any, Throwable, Unit] = logger.log(LogLevel.Error)(s"$caller -> $msg")
}

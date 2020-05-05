package example.modules.services

import cats.implicits._
import example.model.ChatData.User
import example.shared.Dto
import example.shared.Dto.ChatMsg
import example.shared.Dto.ChatUserLeft
import example.shared.Dto.NewChatUser
import example.shared.Dto.UnknownData
import fs2.concurrent.Queue
import io.scalaland.chimney.dsl._
import zio._

object chatService {
  type ChatService = Has[ChatService.Service]

  object ChatService {
    trait Service {
      def createUser(out: Queue[Task[*], Dto.ChatDto]): Task[Dto.ChatUser]
      def handleUserMsg(u: Dto.ChatUser, msg: Dto.ClientMsg): Task[Unit]
      def handleServerMsg(msg: Dto.ServerMsg): Task[Unit]
    }

    val live: ZLayer[Any, Throwable, ChatService] =
      ZLayer.fromEffect(for {
        users     <- Ref.make(Vector[User]())
        idCounter <- Ref.make(1)
      } yield new Service {

        def zioUnit: ZIO[Any, Throwable, Unit] = ZIO.unit // Nothing -> Throwable
        def broadcast(msg: Dto.ChatDto): ZIO[Any, Throwable, Unit] =
          for {
            users <- users.get
            _     <- users.foldLeft(zioUnit)((acc, rcvU) => acc *> rcvU.out.enqueue1(msg))
          } yield ()

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

        def handleUserMsg(u: Dto.ChatUser, msg: Dto.ClientMsg): Task[Unit] = msg match {
          case chatMsg: ChatMsg =>
            val msgToBroadcast = chatMsg.copy(user = u.some)
            broadcast(msgToBroadcast)

          case ud: UnknownData =>
            for {
              users <- users.get
              maybeUser = users.find(_.id == u.id)
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

      })
  }

  def createUser(out: Queue[Task[*], Dto.ChatDto]): ZIO[ChatService, Throwable, Dto.ChatUser] =
    ZIO.accessM[ChatService](_.get.createUser(out))
  def handleUserMsg(u: Dto.ChatUser, msg: Dto.ClientMsg): ZIO[ChatService, Throwable, Unit] =
    ZIO.accessM[ChatService](_.get.handleUserMsg(u, msg))
  def handleServerMsg(msg: Dto.ServerMsg): ZIO[ChatService, Throwable, Unit] =
    ZIO.accessM[ChatService](_.get.handleServerMsg(msg))
}

package example.modules.services

import cats.implicits._
import example.model.ChatData.User
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
import zio.logging.Logging

object chatService {
  type ChatService = Has[ChatService.Service]

  object ChatService {
    trait Service {
      def createUser(out: Queue[Task[*], Dto.ChatDto]): Task[Dto.ChatUser]
      def handleUserMsg(userId: Int, msg: Dto.ClientMsg): Task[Unit]
      def handleServerMsg(msg: Dto.ServerMsg): Task[Unit]
    }

    val live: ZLayer[Any with Logging, Throwable, ChatService] =
      ZLayer.fromServiceM[Logger[String], Any, Throwable, ChatService.Service] { logger =>
        for {
          users     <- Ref.make(Vector[User]())
          idCounter <- Ref.make(1)
        } yield new ChatServiceLive(users, idCounter, logger)
      }
  }

  def createUser(out: Queue[Task[*], Dto.ChatDto]): ZIO[ChatService, Throwable, Dto.ChatUser] =
    ZIO.accessM[ChatService](_.get.createUser(out))
  def handleUserMsg(userId: Int, msg: Dto.ClientMsg): ZIO[ChatService, Throwable, Unit] =
    ZIO.accessM[ChatService](_.get.handleUserMsg(userId, msg))
  def handleServerMsg(msg: Dto.ServerMsg): ZIO[ChatService, Throwable, Unit] =
    ZIO.accessM[ChatService](_.get.handleServerMsg(msg))
}

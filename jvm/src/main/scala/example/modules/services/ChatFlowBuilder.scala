package example.modules.services

import example.modules.services.chatService.ChatService
import fs2.{Pipe, Stream}
import org.http4s.websocket.WebSocketFrame
import zio._
import zio.logging.Logger
import zio.logging.Logging

object chatFlowBuilder {
  type ChatFlowBuilder = Has[ChatFlowBuilder.Service]

  object ChatFlowBuilder {
    case class ChatClientFlow[R](
      out: Stream[Task[*], WebSocketFrame],
      in: Pipe[RIO[R, *], WebSocketFrame, Unit],
      onClose: Task[Unit]
    )

    trait Service {
      def build[R](): RIO[R, ChatClientFlow[R]]
    }

    val live: ZLayer[ChatService with Logging, Nothing, ChatFlowBuilder] =
      ZLayer.fromServices[ChatService.Service, Logger[String], ChatFlowBuilder.Service] { (chatService, logger) =>
        new ChatFlowBuilderLive(chatService, logger)
      }
  }

  def build[R](): ZIO[R with ChatFlowBuilder, Throwable, ChatFlowBuilder.ChatClientFlow[R]] =
    ZIO.accessM[R with ChatFlowBuilder](_.get.build[R]())
}

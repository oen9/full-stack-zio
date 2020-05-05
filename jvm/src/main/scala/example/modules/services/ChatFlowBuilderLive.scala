package example.modules.services

import cats.implicits._
import example.modules.services.chatFlowBuilder.ChatFlowBuilder
import example.modules.services.chatFlowBuilder.ChatFlowBuilder.ChatClientFlow
import example.modules.services.chatService.ChatService
import example.shared.Dto
import example.shared.Dto._
import fs2.concurrent.Queue
import fs2.Pipe
import io.circe.generic.extras.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.websocket.WebSocketFrame
import zio._
import zio.interop.catz._
import zio.logging.Logger
import zio.logging.LogLevel

class ChatFlowBuilderLive(
  chatService: ChatService.Service,
  logger: Logger
) extends ChatFlowBuilder.Service {

  def build[R](): RIO[R, ChatClientFlow[R]] =
    for {
      in           <- Queue.unbounded[RIO[R, *], WebSocketFrame]
      inEndChannel <- Queue.unbounded[Task[*], Option[Unit]]
      out          <- Queue.unbounded[Task[*], Dto.ChatDto]

      user <- chatService.createUser(out)
      _    <- logger.log(LogLevel.Trace)(s"user ${user.id} connected")

      qinLogic = in.dequeue
        .through(handleMsg[R](user))
        .merge(inEndChannel.dequeue)
        .unNoneTerminate
      _ <- (for {
        res <- qinLogic.compile.drain
        _   <- logger.log(LogLevel.Trace)(s"${user.id} Fiber ended")
      } yield ()).forkDaemon

      outStream = out.dequeue.map(toWsFrame)
    } yield ChatClientFlow(outStream, in.enqueue, onClose(user, inEndChannel))

  def onClose(u: Dto.ChatUser, inEndChannel: fs2.concurrent.Queue[Task[*], Option[Unit]]) =
    for {
      _ <- inEndChannel.enqueue1(none)
      _ <- chatService.handleServerMsg(Dto.ChatUserLeft(u))
    } yield ()

  def handleMsg[R](u: Dto.ChatUser): Pipe[RIO[R, *], WebSocketFrame, Option[Unit]] =
    _.collect {
      case WebSocketFrame.Text(msg, _) => fromWsFrame(msg)
    }.evalMap(msg =>
        for {
          _ <- logger.log(LogLevel.Trace)(s"rcv $u : $msg")
          _ <- chatService.handleUserMsg(u, msg)
        } yield msg
      )
      .dropWhile(_ => true)
      .map(_ => ().some)

  def toWsFrame(dto: Dto.ChatDto): WebSocketFrame = WebSocketFrame.Text(dto.asJson.noSpaces)
  def fromWsFrame(msg: String): Dto.ClientMsg     = decode[Dto.ClientMsg](msg).fold(_ => UnknownData(msg), identity)

}

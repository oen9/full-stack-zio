package example.endpoints

import cats.implicits._
import example.modules.services.chatService
import example.modules.services.chatService.ChatService
import example.shared.Dto
import example.shared.Dto._
import fs2.concurrent.Queue
import fs2.Pipe
import io.circe.generic.extras.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import zio._
import zio.interop.catz._
import zio.logging._

object ChatEndpoints {

  def routes[R <: Logging with ChatService]: HttpRoutes[RIO[R, *]] = {
    val dsl = Http4sDsl[RIO[R, *]]
    import dsl._

    def onClose(u: Dto.ChatUser, inEndChannel: fs2.concurrent.Queue[Task[*], Option[Unit]]) =
      for {
        _ <- inEndChannel.enqueue1(none)
        _ <- chatService.handleServerMsg(Dto.ChatUserLeft(u))
      } yield ()

    def handleMsg(u: Dto.ChatUser): Pipe[RIO[R, *], WebSocketFrame, Option[Unit]] =
      _.collect {
        case WebSocketFrame.Text(msg, _) => fromWsFrame(msg)
      }.evalMap(msg =>
          for {
            _ <- logTrace(s"rcv $u : $msg")
            _ <- chatService.handleUserMsg(u, msg)
          } yield msg
        )
        .dropWhile(_ => true)
        .map(_ => ().some)

    def toWsFrame(dto: Dto.ChatDto): WebSocketFrame = WebSocketFrame.Text(dto.asJson.noSpaces)
    def fromWsFrame(msg: String): Dto.ClientMsg     = decode[Dto.ClientMsg](msg).fold(_ => UnknownData(msg), identity)

    HttpRoutes.of {
      case request @ GET -> Root / "chat" =>
        for {
          in           <- Queue.unbounded[RIO[R, *], WebSocketFrame]
          inEndChannel <- Queue.unbounded[Task[*], Option[Unit]]
          out          <- Queue.unbounded[Task[*], Dto.ChatDto]

          user <- chatService.createUser(out)
          _    <- logTrace(s"user ${user.id} connected")

          qinLogic = in.dequeue
            .through(handleMsg(user))
            .merge(inEndChannel.dequeue)
            .unNoneTerminate
          _ <- (for {
            res <- qinLogic.compile.drain
            _   <- logTrace(s"${user.id} Fiber ended")
          } yield ()).forkDaemon

          outStream = out.dequeue.map(toWsFrame)

          ws <- WebSocketBuilder[RIO[R, *]].build(
            outStream,
            in.enqueue,
            onClose = onClose(user, inEndChannel)
          )
        } yield ws
    }
  }
}

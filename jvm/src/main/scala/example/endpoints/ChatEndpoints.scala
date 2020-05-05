package example.endpoints

import example.modules.services.chatFlowBuilder
import example.modules.services.chatFlowBuilder.ChatFlowBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder
import zio._
import zio.interop.catz._
import zio.logging._

object ChatEndpoints {

  def routes[R <: Logging with ChatFlowBuilder]: HttpRoutes[RIO[R, *]] = {
    val dsl = Http4sDsl[RIO[R, *]]
    import dsl._
    HttpRoutes.of {
      case request @ GET -> Root / "chat" =>
        for {
          flowData <- chatFlowBuilder.build[R]()
          ws <- WebSocketBuilder[RIO[R, *]].build(
            flowData.out,
            flowData.in,
            onClose = flowData.onClose
          )
        } yield ws
    }
  }
}

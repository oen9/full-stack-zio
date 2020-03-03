package example

import zio._
import zio.random._
import zio.interop.catz._

import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._

import example.shared.Dto.Foo
import example.shared.Dto.Event

object RestEndpoints {
  import io.circe.generic.extras.Configuration
  implicit val circeConfig = Configuration.default.withDiscriminator("eventType").withDefaults

  def routes[R <: Random]: HttpRoutes[RIO[R, *]] = {
    val dsl = Http4sDsl[RIO[R, *]]
    import dsl._

    HttpRoutes.of[RIO[R, *]] {
      case request@GET -> Root / "json" / "random" => for {
        randomNumber <- nextInt(100)
        response <- Ok((Foo(randomNumber): Event).asJson)
      } yield response
    }
  }
}

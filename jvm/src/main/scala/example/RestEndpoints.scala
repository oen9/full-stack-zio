package example

import zio._
import zio.interop.catz._

import io.circe.generic.extras.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

import example.modules.randomService.getRandom
import example.shared.Dto._
import example.modules.randomService.getRandom
import example.modules.randomService.RandomService

object RestEndpoints {
  def routes[R <: RandomService]: HttpRoutes[RIO[R, *]] = {
    val dsl = Http4sDsl[RIO[R, *]]
    import dsl._

    HttpRoutes.of[RIO[R, *]] {
      case request@GET -> Root / "json" / "random" => for {
        randomNumber <- getRandom
        response <- Ok((Foo(randomNumber): Event).asJson)
      } yield response
    }
  }
}

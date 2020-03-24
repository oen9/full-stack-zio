package example

import cats.implicits._
import zio._
import zio.interop.catz._

import io.circe.generic.extras.auto._
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s._

import example.modules.randomService.getRandom
import example.modules.randomService.getRandom
import example.modules.randomService.RandomService
import example.shared.Dto._

object RestEndpoints {
  val getUserEndpoint = endpoint.get
    .in("json" / "random")
    .out(jsonBody[Foo])

  val getHelloEndpoint = endpoint.get
    .in("hello")
    .out(jsonBody[String])

  val echoEndpoint = endpoint.get
    .in("echo" / path[String]("echo text"))
    .out(stringBody)

  def endpoints = List(
    getUserEndpoint,
    getHelloEndpoint,
    echoEndpoint
  )

  def getUserRoute[R <: RandomService]: HttpRoutes[RIO[R, *]] = getUserEndpoint.toRoutes { _ =>
    for {
      randomNumber <- getRandom
      response = Foo(randomNumber)
    } yield response.asRight[Unit]
  }

  def getHelloRoute[R <: RandomService]: HttpRoutes[RIO[R, *]] = getHelloEndpoint.toRoutes { _ =>
    ZIO.succeed("Hello, World!".asRight[Unit])
  }

  def echoRoute[R <: RandomService]: HttpRoutes[RIO[R, *]] = echoEndpoint.toRoutes { value =>
    ZIO.succeed(value.asRight[Unit])
  }

  def routes[R <: RandomService]: HttpRoutes[RIO[R, *]] =
    getUserRoute[R] <+>
    getHelloRoute <+>
    echoRoute
}

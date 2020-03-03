package example

import cats.implicits._
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestRandom
import zio.test.environment.TestEnvironment
import zio.interop.catz._

import org.http4s._
import org.http4s.implicits._
import example.shared.Dto.Foo
import io.circe.generic.extras.auto._

import example.shared.Dto.Event

object RestEndpointsTest extends DefaultRunnableSpec(
  suite("RestEndpoints")(
    testM("GET /json/random") {
      import RestEndpoints._
      val expected = Vector(Foo(3), Foo(5), Foo(7), Foo(11))
      val req = Request[RIO[TestEnvironment, *]](Method.GET, uri"/json/random")

      def reqRandom() = for {
        resp <- RestEndpoints.routes[TestEnvironment].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnvironment, Event](resp)
      } yield parsedBody

      for {
        _ <- TestRandom.feedInts(expected.map(_.i): _*)
        responses <- (0 until expected.size)
          .toVector
          .map(_ => reqRandom())
          .sequence
          .map(_.flatten)
      } yield assert(responses, equalTo(expected))
    }
  )
)

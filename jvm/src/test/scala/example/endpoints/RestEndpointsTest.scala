package example.endpoints

import cats.implicits._
import zio._
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.environment.TestRandom

import io.circe.generic.extras.auto._
import org.http4s._
import org.http4s.implicits._

import example.endpoints.RestEndpoints
import example.Http4sTestHelper
import example.modules.services.randomService.RandomService
import example.shared.Dto._

object RestEndpointsTest extends DefaultRunnableSpec {
  type TestEnv = TestEnvironment
                  with RandomService

  def spec = suite("RestEndpoints")(
    testM("GET /json/random") {
      val expected = Vector(Foo(3), Foo(5), Foo(7), Foo(11))
      val req = Request[RIO[TestEnv, *]](Method.GET, uri"/json/random")

      def reqRandom() = for {
        resp <- RestEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, Foo](resp)
      } yield parsedBody

      val randomTest = for {
        _ <- TestRandom.feedInts(expected.map(_.i): _*)
        responses <- (0 until expected.size)
          .toVector
          .map(_ => reqRandom())
          .sequence
          .map(_.flatten)
      } yield assert(responses)(equalTo(expected))

      randomTest.provideLayer(
        TestEnvironment.any ++
        RandomService.live
      )
    }
  )
}

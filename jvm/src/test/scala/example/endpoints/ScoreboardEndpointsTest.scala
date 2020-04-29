package example.endpoints

import cats.implicits._
import zio._
import zio.logging.Logging
import zio.test._
import zio.test.Assertion._

import io.circe.generic.extras.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

import example.Http4sTestHelper
import example.modules.services.scoreboardService.ScoreboardService
import example.shared.Dto._
import example.TestEnvs

object ScoreboardEndpointsTest extends DefaultRunnableSpec {
  type TestEnv = ScoreboardService with Logging

  val initData = Vector(
    ScoreboardRecord(id = 1L.some, name = "foo", score = 10),
    ScoreboardRecord(id = 2L.some, name = "bar", score = 20),
    ScoreboardRecord(id = 3L.some, name = "baz", score = 5)
  )

  def spec = suite("ScoreboardEndpoints")(
    testM("GET /scoreboard") {
      val req = Request[RIO[TestEnv, *]](Method.GET, uri"/scoreboard")

      val program = for {
        response   <- ScoreboardEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, Vector[ScoreboardRecord]](response)
      } yield assert(parsedBody)(isSome(equalTo(initData)))

      program.provideLayer(
        TestEnvs.logging ++ ScoreboardService.test(initData)
      )
    },
    testM("POST /scoreboard") {
      val postData = ScoreboardRecord(id = None, name = "foo", score = 10)
      val req = Request[RIO[TestEnv, *]](Method.POST, uri"/scoreboard")
        .withEntity(postData.asJson)

      val program = for {
        response   <- ScoreboardEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, ScoreboardRecord](response)
        expected = postData.copy(id = parsedBody.flatMap(_.id))
      } yield assert(response.map(_.status))(isSome(equalTo(Status.Created))) &&
        assert(parsedBody.flatMap(_.id))(isSome(anything)) &&
        assert(parsedBody)(isSome(equalTo(expected)))

      program.provideLayer(
        TestEnvs.logging ++ ScoreboardService.test()
      )
    },
    testM("POST /scoreboard bad request") {
      val req = Request[RIO[TestEnv, *]](Method.POST, uri"/scoreboard")
        .withEntity("some req")

      val program = for {
        response <- ScoreboardEndpoints.routes[TestEnv].run(req).value
      } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

      program.provideLayer(
        TestEnvs.logging ++ ScoreboardService.test()
      )
    },
    testM("DELETE /scoreboard") {
      val req = Request[RIO[TestEnv, *]](Method.DELETE, uri"/scoreboard")

      val program = for {
        response <- ScoreboardEndpoints.routes[TestEnv].run(req).value
      } yield assert(response.map(_.status))(isSome(equalTo(Status.NoContent)))

      program.provideLayer(
        TestEnvs.logging ++ ScoreboardService.test()
      )
    }
  )
}

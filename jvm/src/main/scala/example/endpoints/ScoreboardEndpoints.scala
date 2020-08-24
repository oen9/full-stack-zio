package example.endpoints

import cats.implicits._
import zio._
import zio.interop.catz._
import zio.logging._

import io.circe.generic.extras.auto._
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s._

import example.model.Errors._
import example.modules.services.scoreboardService
import example.modules.services.scoreboardService.ScoreboardService
import example.shared.Dto._

object ScoreboardEndpoints {
  val unexpectedError = oneOf(
    statusMapping(StatusCode.InternalServerError, jsonBody[UnknownError].description("unknown default error"))
  )

  val listScores = endpoint.get
    .description("List all scores sorted by score")
    .in("scoreboard")
    .errorOut(unexpectedError)
    .out(
      jsonBody[Vector[ScoreboardRecord]].example(
        Vector(ScoreboardRecord(id = 2L.some, name = "bar", 100), ScoreboardRecord(id = 1L.some, name = "foo", 50))
      )
    )

  val createNew = endpoint.post
    .description("Create new score record")
    .in("scoreboard")
    .in(jsonBody[ScoreboardRecord].example(ScoreboardRecord()))
    .errorOut(unexpectedError)
    .out(jsonBody[ScoreboardRecord].example(ScoreboardRecord(id = 3L.some)))
    .out(statusCode(StatusCode.Created))

  val deleteAll = endpoint.delete
    .description("Delete all scores")
    .in("scoreboard")
    .errorOut(unexpectedError)
    .out(statusCode(StatusCode.NoContent))

  def endpoints = List(
    listScores,
    createNew,
    deleteAll
  )

  // format: off
  def routes[R <: ScoreboardService with Logging]: HttpRoutes[RIO[R, *]] =
    (
      listScores.toRoutes { _ =>
          handleUnexpectedError(scoreboardService.listScores())
      }: HttpRoutes[RIO[R, *]] // TODO find a better way
    ) <+> createNew.toRoutes { toCreate =>
      handleUnexpectedError(scoreboardService.addNew(toCreate))
    } <+> deleteAll.toRoutes { _ =>
      handleUnexpectedError(scoreboardService.deleteAll())
    }
  // format: on

  private def handleUnexpectedError[R <: Logging, A](result: ZIO[R, Throwable, A]): URIO[R, Either[UnknownError, A]] =
    result.foldM(
      {
        case unknown =>
          for {
            _ <- log.throwable("unknown error", unknown)
          } yield UnknownError(s"Something went wrong. Check logs for more info").asLeft
      },
      succ => ZIO.succeed(succ.asRight)
    )
}

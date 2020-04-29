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
import example.modules.services.todoService
import example.modules.services.todoService.TodoService
import example.shared.Dto._

object TodoEndpoints {
  val exampleId = "5e7ca3231200001200268a81"

  val allErrorsOut = oneOf(
    statusMapping(StatusCode.BadRequest, jsonBody[BadRequest].description("bad id")),
    statusMapping(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
    statusMapping(StatusCode.InternalServerError, jsonBody[UnknownError].description("unknown default error"))
  )

  val unexpectedError = oneOf(
    statusMapping(StatusCode.InternalServerError, jsonBody[UnknownError].description("unknown default error"))
  )

  val getAllTodos = endpoint.get
    .in("todos")
    .errorOut(unexpectedError)
    .out(jsonBody[List[TodoTask]].example(List(TodoTask(exampleId.some), TodoTask(exampleId.some))))

  val createNew = endpoint.post
    .in("todos")
    .in(jsonBody[TodoTask].example(TodoTask()))
    .errorOut(unexpectedError)
    .out(jsonBody[String].example(exampleId))
    .out(statusCode(StatusCode.Created))

  val switchStatus = endpoint.get
    .in("todos" / path[String]("id").example(exampleId) / "switch")
    .errorOut(allErrorsOut)
    .out(jsonBody[TodoStatus].example(Pending))

  val deleteTodo = endpoint.delete
    .in("todos" / path[String]("id").example(exampleId))
    .errorOut(allErrorsOut)
    .out(statusCode(StatusCode.NoContent))

  def endpoints = List(
    getAllTodos,
    createNew,
    switchStatus,
    deleteTodo
  )

  def routes[R <: TodoService with Logging]: HttpRoutes[RIO[R, *]] =
    (
      getAllTodos.toRoutes(_ => handleUnexpectedError(todoService.getAll)): HttpRoutes[
        RIO[R, *]
      ] // TODO find a better way
    ) <+> createNew.toRoutes(toCreate => handleUnexpectedError(todoService.createNew(toCreate))) <+> switchStatus.toRoutes {
      id => handleError(todoService.switchStatus(id))
    } <+> deleteTodo.toRoutes(id => handleError(todoService.deleteTodo(id)))

  private def handleError[R <: Logging, A](
    result: ZIO[R, Throwable, A]
  ): URIO[R, Either[ErrorInfo with Product with Serializable, A]] = result.foldM(
    {
      case WrongMongoId(msg)     => ZIO.succeed(BadRequest(msg).asLeft)
      case TodoTaskNotFound(msg) => ZIO.succeed(NotFound(msg).asLeft)

      case unknown =>
        for {
          _ <- logThrowable(unknown)
        } yield UnknownError(s"Something went wrong. Check logs for more info").asLeft
    },
    succ => ZIO.succeed(succ.asRight)
  )

  private def handleUnexpectedError[R <: Logging, A](
    result: ZIO[R, Throwable, A]
  ): URIO[R, Either[UnknownError with Product with Serializable, A]] = result.foldM(
    {
      case unknown =>
        for {
          _ <- logThrowable(unknown)
        } yield UnknownError(s"Something went wrong. Check logs for more info").asLeft
    },
    succ => ZIO.succeed(succ.asRight)
  )
}

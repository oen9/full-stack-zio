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
import example.shared.Dto._
import example.shared.Dto
import example.modules.services.auth.authService.AuthService
import example.modules.services.auth.authService

object AuthEndpoints {
  val exampleToken = "test"

  val allErrorsOut = oneOf(
    statusMapping(StatusCode.Unauthorized, jsonBody[Unauthorized].description("wrong token/credentials")),
    statusMapping(StatusCode.Conflict, jsonBody[Conflict].description("conflict in request with existing data")),
    statusMapping(StatusCode.InternalServerError, jsonBody[UnknownError].description("unknown default error"))
  )

  val apiKey     = auth.apiKey(header[Dto.Token]("TOKEN").example(exampleToken))
  val userDtoOut = jsonBody[Dto.User].example(Dto.User(1L, "foo", exampleToken))
  val authDtoIn  = jsonBody[Dto.AuthCredentials].example(Dto.AuthCredentials("foo", "bar"))

  val getUser = endpoint.get
    .description("get user info by token")
    .in("auth" / "user")
    .in(apiKey)
    .errorOut(allErrorsOut)
    .out(userDtoOut)

  val createUser = endpoint.post
    .description("create new user")
    .in("auth" / "user")
    .in(authDtoIn)
    .errorOut(allErrorsOut)
    .out(userDtoOut)
    .out(statusCode(StatusCode.Created))

  val generateNewToken = endpoint.post
    .description("generate new token")
    .in("auth")
    .in(authDtoIn)
    .errorOut(allErrorsOut)
    .out(userDtoOut)

  val securedText = endpoint.get
    .description("get a super secure info for specific user")
    .in("auth" / "secured")
    .in(apiKey)
    .errorOut(allErrorsOut)
    .out(jsonBody[String].example("Secret text"))

  def endpoints = List(
    getUser,
    createUser,
    generateNewToken,
    securedText
  )

  def routes[R <: AuthService with Logging]: HttpRoutes[RIO[R, *]] =
    (
      getUser.toRoutes(token => handleError(authService.getUser(token))): HttpRoutes[RIO[R, *]]
    ) <+> createUser.toRoutes(cred => handleError(authService.createUser(cred))) <+> generateNewToken.toRoutes { cred =>
      handleError(authService.generateNewToken(cred))
    } <+> securedText.toRoutes(token => handleError(authService.getUser(token) >>= authService.secretText))

  private def handleError[R <: Logging, A](result: ZIO[R, Throwable, A]): URIO[R, Either[ErrorInfo, A]] = result.foldM(
    {
      case TokenNotFound(msg)       => ZIO.succeed(Unauthorized(msg).asLeft)
      case AuthenticationError(msg) => ZIO.succeed(Unauthorized(msg).asLeft)
      case UserExists(msg)          => ZIO.succeed(Conflict(msg).asLeft)

      case unknown =>
        for {
          _ <- logThrowable(unknown)
        } yield UnknownError(s"Something went wrong. Check logs for more info").asLeft
    },
    succ => ZIO.succeed(succ.asRight)
  )
}

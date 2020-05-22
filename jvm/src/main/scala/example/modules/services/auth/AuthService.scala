package example.modules.services.auth

import example.model.Errors.AuthenticationError
import example.model.Errors.UserExists
import example.modules.db.userRepository.UserRepository
import example.modules.services.cryptoService.CryptoService
import example.shared.Dto
import io.scalaland.chimney.dsl._
import zio._
import zio.logging.Logger
import zio.logging.Logging

object authService {
  type AuthService = Has[AuthService.Service]

  object AuthService {
    trait Service {
      def getUser(token: Dto.Token): Task[Dto.User]
      def generateNewToken(cred: Dto.AuthCredentials): Task[Dto.User]
      def createUser(cred: Dto.AuthCredentials): Task[Dto.User]
      def secretText(user: Dto.User): Task[String]
    }

    val live: ZLayer[UserRepository with Logging with CryptoService, Nothing, AuthService] =
      ZLayer.fromServices[UserRepository.Service, Logger[String], CryptoService.Service, AuthService.Service] {
        (userRepository, logger, cryptoService) => new AuthServiceLive(userRepository, logger, cryptoService)
      }

    def test(
      data: Vector[Dto.User] = Vector(),
      newToken: String = "newToken",
      secretReturn: String = "Super secret text"
    ) =
      ZLayer.succeed(new Service {

        def getUser(token: Dto.Token): Task[Dto.User] =
          ZIO
            .fromOption(
              data
                .find(_.token == token)
            )
            .mapError(_ => AuthenticationError("wrong token"))

        def generateNewToken(cred: Dto.AuthCredentials): Task[Dto.User] =
          ZIO
            .fromOption(
              data
                .find(_.name == cred.name)
                .map(_.copy(token = newToken))
            )
            .mapError(_ => AuthenticationError("wrong name/password"))

        def createUser(cred: Dto.AuthCredentials): Task[Dto.User] = {
          val succ: ZIO[Any, Throwable, Dto.User] = ZIO.succeed(
            cred
              .into[Dto.User]
              .withFieldComputed(_.id, _ => 1L)
              .withFieldComputed(_.token, _ => newToken)
              .transform
          )
          val fail = ZIO.fail(UserExists(s"user ${cred.name} exists"))
          data
            .find(_.name == cred.name)
            .fold(succ)(_ => fail)
        }

        def secretText(user: Dto.User): Task[String] = ZIO.succeed(secretReturn)
      })
  }

  def getUser(token: Dto.Token): ZIO[AuthService, Throwable, Dto.User] =
    ZIO.accessM[AuthService](_.get.getUser(token))
  def generateNewToken(cred: Dto.AuthCredentials): ZIO[AuthService, Throwable, Dto.User] =
    ZIO.accessM[AuthService](_.get.generateNewToken(cred))
  def createUser(cred: Dto.AuthCredentials): ZIO[AuthService, Throwable, Dto.User] =
    ZIO.accessM[AuthService](_.get.createUser(cred))
  def secretText(user: Dto.User): ZIO[AuthService, Throwable, String] =
    ZIO.accessM[AuthService](_.get.secretText(user))
}

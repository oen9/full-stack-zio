package example.modules.services.auth

import example.modules.db.userRepository.UserRepository
import example.modules.services.cryptoService.CryptoService
import example.shared.Dto
import io.scalaland.chimney.dsl._
import zio._
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
      ZLayer.fromServices[UserRepository.Service, Logging.Service, CryptoService.Service, AuthService.Service] {
        (userRepository, logging, cryptoService) => new AuthServiceLive(userRepository, logging.logger, cryptoService)
      }

    def test(data: Vector[Dto.User]) = ZLayer.succeed(new Service {
      def getUser(token: Dto.Token): Task[Dto.User] = ZIO.succeed(data.find(_.token == token).get)
      def generateNewToken(cred: Dto.AuthCredentials): Task[Dto.User] = ZIO.succeed(
        data
          .find(_.name == cred.name)
          .map(_.copy(token = "newToken"))
          .get
      )
      def createUser(cred: Dto.AuthCredentials): Task[Dto.User] = ZIO.succeed(
        cred
          .into[Dto.User]
          .withFieldComputed(_.id, _ => 1L)
          .withFieldComputed(_.token, _ => "newNewToken")
          .transform
      )
      def secretText(user: Dto.User): Task[String] =
        ZIO.succeed(s"This is super secure message for ${user.name}!")
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

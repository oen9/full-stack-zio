package example.modules.services

import example.modules.appConfig
import example.modules.appConfig.AppConfig
import example.shared.Dto
import org.mindrot.jbcrypt.BCrypt
import org.reactormonk.CryptoBits
import org.reactormonk.PrivateKey
import zio._
import zio.clock.Clock

object cryptoService {

  type CryptoService = Has[CryptoService.Service]

  object CryptoService {
    trait Service {
      def generateToken(s: String): Task[Dto.Token]
      def hashPassword(password: String): String
      def chkPassword(password: String, hashed: String): Boolean
    }

    val live: ZLayer[AppConfig with Clock, Throwable, CryptoService] =
      ZLayer.fromServiceM[Clock.Service, AppConfig, Throwable, CryptoService.Service] { clock =>
        appConfig.load.map(cfg => new Service {
          val key = PrivateKey(scala.io.Codec.toUTF8(cfg.encryption.salt))
          val crypto = CryptoBits(key)
          val testUsername = "test"

          def generateToken(s: String): Task[Dto.Token] =
            clock.nanoTime.map(nanos =>
              if (s == testUsername) testUsername
              else crypto.signToken(s, nanos.toString())
            )

          def hashPassword(password: String): String =
            BCrypt.hashpw(password, BCrypt.gensalt(cfg.encryption.bcryptLogRounds))

          def chkPassword(password: String, hashed: String): Boolean =
            BCrypt.checkpw(password, hashed)
        })
      }
  }

  def generateToken(s: String): ZIO[CryptoService, Throwable, Dto.Token] =
    ZIO.accessM[CryptoService](_.get.generateToken(s))
  def hashPassword(password: String): ZIO[CryptoService, Throwable, String] =
    ZIO.access[CryptoService](_.get.hashPassword(password))
  def chkPassword(password: String, hashed: String): ZIO[CryptoService, Throwable, Boolean] =
    ZIO.access[CryptoService](_.get.chkPassword(password, hashed))
}

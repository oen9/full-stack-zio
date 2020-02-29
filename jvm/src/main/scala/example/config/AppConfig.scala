package example.config

import cats.effect.Sync
import cats.implicits._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import pureconfig.ConfigSource

class AppConfigException(failures: ConfigReaderFailures) extends RuntimeException(failures.toList.mkString(" "))

case class Http(port: Int, host: String)
case class AppConfig(http: Http, assets: String)

object AppConfig {
  def read[F[_] : Sync](): F[AppConfig] = {
    Sync[F].delay(ConfigSource.default.load[AppConfig]).flatMap {
      case Right(conf) => Sync[F].pure(conf)
      case Left(e) => Sync[F].raiseError(new AppConfigException(e))
    }
  }
}

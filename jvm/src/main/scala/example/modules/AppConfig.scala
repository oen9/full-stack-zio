package example.modules

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

case class Http(port: Int, host: String)
case class Mongo(uri: String)
case class Postgres(url: String)
case class AppConfigData(http: Http, mongo: Mongo, postgres: Postgres, assets: String)

object appConfig {
  type AppConfig = Has[AppConfig.Service]

  object AppConfig {
    trait  Service {
      def load: Task[AppConfigData]
    }

    val live: Layer[Nothing, AppConfig] = ZLayer.succeed(new Service {
      def load: ZIO[Any, Throwable, AppConfigData] = Task.effect(ConfigSource.default.loadOrThrow[AppConfigData])
    })

    val test: Layer[Nothing, AppConfig] = ZLayer.succeed(new Service {
      def load: ZIO[Any, Throwable, AppConfigData] = ZIO.effectTotal(AppConfigData(
        Http(8080, "localhost"),
        Mongo("mongo://test:test@localhost/test"),
        Postgres("postgres://test:test@localhost:5432/test"),
        "/tmp")
      )
    })
  }

  def load: ZIO[AppConfig, Throwable, AppConfigData] =
    ZIO.accessM[AppConfig](_.get.load)

}

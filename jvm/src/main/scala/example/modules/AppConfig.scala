package example.modules

import com.softwaremill.quicklens._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

case class Http(port: Int, host: String)
case class Mongo(uri: String)
case class SQLDB(url: String, driver: String, username: String = "", password: String = "")
case class Encryption(salt: String, bcryptLogRounds: Int)
case class AppConfigData(http: Http, mongo: Mongo, sqldb: SQLDB, encryption: Encryption, assets: String)

object appConfig {
  type AppConfig = Has[AppConfig.Service]

  object AppConfig {
    trait  Service {
      def load: Task[AppConfigData]
    }

    val live: Layer[Nothing, AppConfig] = ZLayer.succeed(new Service {
      def load: ZIO[Any, Throwable, AppConfigData] = Task.effect(ConfigSource.default.loadOrThrow[AppConfigData])
    })

    // actually we want to use application.conf from test so `live` for tests is better
    val test: Layer[Nothing, AppConfig] = ZLayer.succeed(new Service {
      def load: ZIO[Any, Throwable, AppConfigData] = ZIO.effectTotal(AppConfigData(
        Http(8080, "localhost"),
        Mongo("mongo://test:test@localhost/test"),
        SQLDB(url = "postgres://test:test@localhost:5432/test", driver = "postgres"),
        Encryption(salt = "super-secret", bcryptLogRounds = 10),
        "/tmp")
      )
    })

    def test(h2DbName: String = "test"): Layer[Nothing, AppConfig] =
      live >>> ZLayer.fromService[AppConfig.Service, AppConfig.Service](appConfig => new Service {
        def load: zio.Task[AppConfigData] = appConfig.load.map(_.modify(_.sqldb.url).setTo(
          s"jdbc:h2:mem:$h2DbName;DB_CLOSE_DELAY=-1"
        ))
      })
  }

  def load: ZIO[AppConfig, Throwable, AppConfigData] =
    ZIO.accessM[AppConfig](_.get.load)

}

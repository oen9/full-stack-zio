package example.config

import pureconfig.generic.auto._
import pureconfig.ConfigSource
import zio.Task
import zio.macros.annotation.accessible
import zio.macros.annotation.mockable
import zio.ZIO

case class Http(port: Int, host: String)
case class AppConfigData(http: Http, assets: String)

@accessible(">")
@mockable
trait AppConfig {
  val appConfig: AppConfig.Service[Any]
}

object AppConfig {
  trait Service[R] {
    def load: ZIO[R, Throwable, AppConfigData]
  }

  trait Live extends AppConfig {
    val appConfig: Service[Any] = new Service[Any] {
      def load: ZIO[Any,Throwable,AppConfigData] = Task.effect(ConfigSource.default.loadOrThrow[AppConfigData])
    }
  }

  trait Test extends AppConfig {
    val appConfig: Service[Any] = new Service[Any] {
      def load: ZIO[Any,Throwable,AppConfigData] = ZIO.effectTotal(AppConfigData(Http(8080, "localhost"), "/tmp"))
    }
  }

}

package example

import zio._
import zio.blocking.Blocking
import zio.logging._
import zio.interop.catz._
import cats.implicits._

import org.http4s.server.middleware.CORSConfig
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS

import example.config.appConfig
import example.modules.randomService
import java.io.StringWriter
import java.io.PrintWriter
import scala.concurrent.duration._

object Hello extends App {
  type AppEnv = ZEnv
                  with appConfig.AppConfig
                  with randomService.RandomService
                  with Logging
  type AppTask[A] = ZIO[AppEnv, Throwable, A]

  def run(args: List[String]): ZIO[zio.ZEnv,Nothing,Int] = {
    app
      .flatMapError {
        case e: Throwable =>
          val sw = new StringWriter
          e.printStackTrace(new PrintWriter(sw))
          zio.console.putStrLn(sw.toString())
      }
      .provideCustomLayer(
        slf4j.Slf4jLogger.make((_, msg) => msg) ++
        randomService.RandomService.live ++
        appConfig.AppConfig.live)
      .fold(_ => 1, _ => 0)
  }

  def app(): ZIO[AppEnv, Throwable, Unit] = for {
    conf <- appConfig.load

    originConfig = CORSConfig(
      anyOrigin = true,
      allowCredentials = false,
      maxAge = 1.day.toSeconds)

    ec <- ZIO.accessM[Blocking](b => ZIO.succeed(b.get.blockingExecutor))
    catsBlocker = cats.effect.Blocker.liftExecutionContext(ec.asEC)

    httpApp = (
      RestEndpoints.routes[AppEnv]
      <+> StaticEndpoints.routes[AppEnv](conf.assets, catsBlocker)
    ).orNotFound

    server <- ZIO.runtime[AppEnv].flatMap { implicit rts =>
      BlazeServerBuilder[AppTask]
        .bindHttp(conf.http.port, conf.http.host)
        .withHttpApp(CORS(httpApp))
        .serve
        .compile
        .drain
    }
  } yield server

}

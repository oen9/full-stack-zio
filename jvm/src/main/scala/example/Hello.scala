package example


import zio._
import zio.clock.Clock
import zio.random.Random
import zio.blocking.Blocking
import zio.interop.catz._
import cats.implicits._

import org.http4s.server.middleware.CORSConfig
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS

import example.config.AppConfig
import java.io.StringWriter
import java.io.PrintWriter
import scala.concurrent.duration._

object Hello extends App {
  type AppEnv = AppConfig with Blocking with Random with Clock
  type AppTask[A] = ZIO[AppEnv, Throwable, A]

  def run(args: List[String]): ZIO[zio.ZEnv,Nothing,Int] = {
    def createLiveEnv(zenv: zio.ZEnv): AppEnv = new AppConfig.Live
                                                  with Blocking
                                                  with Random
                                                  with Clock {
      val blocking: Blocking.Service[Any] = zenv.blocking
      val clock: Clock.Service[Any] = zenv.clock
      val random: Random.Service[Any] = zenv.random
    }

    app
      .provideSome[zio.ZEnv](createLiveEnv)
      .flatMapError {
        case e: Throwable =>
          val sw = new StringWriter
          e.printStackTrace(new PrintWriter(sw))
          zio.console.putStrLn(sw.toString())
      }
      .fold(_ => 1, _ => 0)
  }

  def app(): ZIO[AppEnv, Throwable, Unit] = for {
    conf <- AppConfig.>.load

    originConfig = CORSConfig(
      anyOrigin = true,
      allowCredentials = false,
      maxAge = 1.day.toSeconds)

    ec <- zio.blocking.blockingExecutor 
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

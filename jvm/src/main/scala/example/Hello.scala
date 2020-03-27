package example

import cats.implicits._
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.logging._

import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

import example.endpoints.RestEndpoints
import example.endpoints.StaticEndpoints
import example.endpoints.TodoEndpoints
import example.modules.appConfig
import example.modules.db.MongoConn
import example.modules.db.todoRepository
import example.modules.services.randomService
import example.modules.services.todoService
import java.io.PrintWriter
import java.io.StringWriter
import scala.concurrent.duration._

object Hello extends App {
  type AppEnv = ZEnv
                  with appConfig.AppConfig
                  with randomService.RandomService
                  with todoService.TodoService
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
      .provideCustomLayer{
        val appConf = appConfig.AppConfig.live
        val logging = slf4j.Slf4jLogger.make((_, msg) => msg)

        val mongoConf = appConf >>> MongoConn.live
        val todoRepo = (mongoConf ++ logging) >>> todoRepository.TodoRepository.live
        val todoServ = todoRepo >>> todoService.TodoService.live

        logging ++
        appConf ++
        todoServ ++
        randomService.RandomService.live
      }
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

    yamlDocs = (
      RestEndpoints.endpoints
      ++ TodoEndpoints.endpoints
    ).toOpenAPI("full-stack-zio", "0.1.0").toYaml

    httpApp = (
      RestEndpoints.routes[AppEnv]
      <+> TodoEndpoints.routes[AppEnv]
      <+> new SwaggerHttp4s(yamlDocs).routes[RIO[AppEnv, *]]
      <+> StaticEndpoints.routes[AppEnv](conf.assets, catsBlocker)
    ).orNotFound

    server <- ZIO.runtime[AppEnv].flatMap { implicit rts =>
      BlazeServerBuilder[AppTask]
        .bindHttp(conf.http.port, conf.http.host)
        .withHttpApp(CORS(httpApp, originConfig))
        .serve
        .compile
        .drain
    }
  } yield server
}

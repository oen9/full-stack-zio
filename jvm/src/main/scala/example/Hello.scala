package example

import cats.implicits._
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.logging._

import caliban.Http4sAdapter
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.Router
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

import example.endpoints.AuthEndpoints
import example.endpoints.ChatEndpoints
import example.endpoints.RestEndpoints
import example.endpoints.ScoreboardEndpoints
import example.endpoints.StaticEndpoints
import example.endpoints.TodoEndpoints
import example.model.GQLData
import example.modules.appConfig
import example.modules.db.doobieTransactor
import example.modules.db.flywayHandler
import example.modules.db.MongoConn
import example.modules.db.scoreboardRepository
import example.modules.db.todoRepository
import example.modules.db.userRepository
import example.modules.services.auth.authService
import example.modules.services.chatFlowBuilder
import example.modules.services.chatService
import example.modules.services.cryptoService
import example.modules.services.randomService
import example.modules.services.scoreboardService
import example.modules.services.todoService
import java.io.PrintWriter
import java.io.StringWriter
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import zio.clock.Clock

object Hello extends App {
  type AppEnv = ZEnv
    with appConfig.AppConfig
    with randomService.RandomService
    with todoService.TodoService
    with scoreboardService.ScoreboardService
    with authService.AuthService
    with chatService.ChatService
    with chatFlowBuilder.ChatFlowBuilder
    with Logging

  type AppTask[A] = ZIO[AppEnv, Throwable, A]

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    app().provideCustomLayer {
      val appConf    = appConfig.AppConfig.live
      val logging    = slf4j.Slf4jLogger.make((_, msg) => msg)
      val randomServ = randomService.RandomService.live

      val mongoConf = appConf >>> MongoConn.live
      val todoRepo  = (mongoConf ++ logging) >>> todoRepository.TodoRepository.live
      val todoServ  = todoRepo >>> todoService.TodoService.live

      val flyway     = appConf >>> flywayHandler.FlywayHandler.live
      val doobieTran = (Blocking.any ++ appConf ++ flyway) >>> doobieTransactor.live

      val scoreboardRepo = doobieTran >>> scoreboardRepository.ScoreboardRepository.live
      val scoreServ      = (scoreboardRepo ++ logging) >>> scoreboardService.ScoreboardService.live

      val cryptoServ = (appConf ++ Clock.any) >>> cryptoService.CryptoService.live
      val userRepo   = doobieTran >>> userRepository.UserRepository.live
      val authServ   = (userRepo ++ logging ++ cryptoServ) >>> authService.AuthService.live

      val chatServ     = logging >>> chatService.ChatService.live
      val chatFlowBuil = (chatServ ++ logging) >>> chatFlowBuilder.ChatFlowBuilder.live

      logging ++
        appConf ++
        todoServ ++
        scoreServ ++
        authServ ++
        chatServ ++
        chatFlowBuil ++
        randomServ
    }.flatMapError {
      case e: Throwable =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        zio.console.putStrLnErr(sw.toString()).orElse(ZIO.unit)
    }.fold(_ => ExitCode.failure, _ => ExitCode.success)

  def app(): ZIO[AppEnv, Throwable, Unit] =
    for {
      conf <- appConfig.load

      originConfig = CORSConfig(anyOrigin = true, allowCredentials = false, maxAge = 1.day.toSeconds)

      ec <- ZIO.accessM[Blocking](b => ZIO.succeed(b.get.blockingExecutor.asEC))
      catsBlocker = cats.effect.Blocker.liftExecutionContext(ec)

      gqlInterpreter <- GQLData.api.interpreter
      yamlDocs = (
        RestEndpoints.endpoints
          ++ TodoEndpoints.endpoints
          ++ ScoreboardEndpoints.endpoints
          ++ AuthEndpoints.endpoints
      ).toOpenAPI("full-stack-zio", "0.1.0").toYaml

      httpApp = (
        RestEndpoints.routes[AppEnv]
          <+> TodoEndpoints.routes[AppEnv]
          <+> ScoreboardEndpoints.routes[AppEnv]
          <+> AuthEndpoints.routes[AppEnv]
          <+> new SwaggerHttp4s(yamlDocs).routes[RIO[AppEnv, *]]
          <+> ChatEndpoints.routes[AppEnv]
          <+> StaticEndpoints.routes[AppEnv](conf.assets, catsBlocker, GQLData.api.render)
          <+> Router("/api/graphql" -> Http4sAdapter.makeHttpService(gqlInterpreter))
      ).orNotFound

      server <- ZIO.runtime[AppEnv].flatMap { implicit rts =>
        BlazeServerBuilder[AppTask](ExecutionContext.global)
          .bindHttp(conf.http.port, conf.http.host)
          .withHttpApp(CORS(httpApp, originConfig))
          .serve
          .compile
          .drain
      }
    } yield server
}

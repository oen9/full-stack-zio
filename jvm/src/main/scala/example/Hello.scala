package example

import cats.effect._
import cats.implicits._
import example.config.AppConfig
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.CORSConfig
import scala.concurrent.duration._
import org.http4s.server.middleware.CORS

import org.http4s.server.Server

object Hello extends IOApp {

  val originConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = false,
    maxAge = 1.day.toSeconds)

  override def run(args: List[String]): IO[ExitCode] = {
    createServer[IO]().use(_ => IO.never).as(ExitCode.Success)
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](): Resource[F, Server[F]] = {
    for {
      conf <- Resource.liftF(AppConfig.read())
      blocker <- Blocker[F]
      staticEndpoints = StaticEndpoints[F](conf.assets, blocker)
      restEndpoints = RestEndpoints[F]()
      httpApp = (staticEndpoints.endpoints() <+> restEndpoints.endpoints()).orNotFound
      server <- BlazeServerBuilder[F]
        .bindHttp(conf.http.port, conf.http.host)
        .withHttpApp(CORS(httpApp))
        .resource
    } yield server
  }
}

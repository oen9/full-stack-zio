package example

import zio._
import zio.interop.catz._
import cats.effect.Blocker

import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, StaticFile}

object StaticEndpoints {

  def routes[R](assetsPath: String, catsBlocker: Blocker): HttpRoutes[RIO[R, *]] = {
    val dsl = Http4sDsl[RIO[R, *]]
    import dsl._

    def static(file: String, catsBlocker: Blocker, request: Request[RIO[R, *]]) =
      StaticFile.fromResource("/" + file, catsBlocker, Some(request)).getOrElseF(NotFound())

    def staticAssets(file: String, catsBlocker: Blocker, request: Request[RIO[R, *]]) =
      StaticFile.fromString(s"$assetsPath/$file", catsBlocker, Some(request)).getOrElseF(NotFound())

    HttpRoutes.of[RIO[R, *]] {
      case request@GET -> Root =>
        static("index.html", catsBlocker, request)

      case request@GET -> Root / path if List(".js", ".css", ".map", ".html", ".ico").exists(path.endsWith) =>
        static(path, catsBlocker, request)

      case request@GET -> "front-res" /: path =>
        val fullPath = "front-res/" + path.toList.mkString("/")
        static(fullPath, catsBlocker, request)

      case request@GET -> "assets" /: path =>
        val fullPath = path.toList.mkString("/")
        staticAssets(fullPath, catsBlocker, request)
    }
  }

}

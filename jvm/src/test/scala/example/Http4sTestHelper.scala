package example

import cats.implicits._
import zio._
import zio.interop.catz._

import io.circe.Decoder
import io.circe.parser.decode
import org.http4s._

object Http4sTestHelper {
  def parseBody[R, A](resp: Option[Response[RIO[R, *]]])
                     (implicit decoder: Decoder[A]): RIO[R, Option[A]] = for {
    body <- resp.map(_
      .body
      .compile
      .toVector
      .map(x => x.map(_.toChar).mkString(""))
    ).sequence
    parsedBody = body.flatMap(b => decode[A](b).toOption)
  } yield parsedBody
}

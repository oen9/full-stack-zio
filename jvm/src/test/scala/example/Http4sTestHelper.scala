package example

import cats.implicits._
import zio._
import zio.interop.catz._

import org.http4s._
import io.circe.parser.decode
import io.circe.Decoder

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

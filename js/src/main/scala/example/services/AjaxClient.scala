package example.services

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest
import scala.util.Try
import example.shared.Dto.Foo
import scala.util.Success
import scala.util.Failure

object AjaxClient {
  val JSON_TYPE = Map("Content-Type" -> "application/json")
  val baseUrl = ""
  // val baseUrl = "http://localhost:8080" // for dev

  import scala.concurrent.ExecutionContext.Implicits.global

  def getRandom = {
    Ajax.get(
      url = s"$baseUrl/json/random",
      headers = JSON_TYPE
    ).transform(decodeFoo)
  }

  private[this] def decodeFoo(t: Try[XMLHttpRequest]): Try[Foo] = {
    import example.shared.Dto._
    //import io.circe.generic.extras.auto._
    import io.circe.parser.decode
    import io.circe.{ Decoder, HCursor }
    implicit val decodeFoo: Decoder[Foo] = new Decoder[Foo] {
      final def apply(c: HCursor): Decoder.Result[Foo] = for {
          i <- c.downField("i").as[Int]
        } yield {
          new Foo(i)
        }
    }
    t match {
      case Success(req) => decode[Foo](req.responseText).toTry
      case Failure(e) => Failure(e)
    }
  }
}

package example.services

import example.shared.Dto.Foo
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import example.shared.Dto._
import io.circe.Decoder
import io.circe.generic.extras.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.scalajs.dom.ext.AjaxException
import scala.scalajs.LinkingInfo

object AjaxClient {
  val JSON_TYPE                 = Map("Content-Type" -> "application/json")
  def authHeader(token: String) = Map("TOKEN" -> token)
  val baseUrl                   = if (LinkingInfo.developmentMode) "http://localhost:8080" else ""

  import scala.concurrent.ExecutionContext.Implicits.global

  def getRandom =
    Ajax
      .get(
        url = s"$baseUrl/json/random",
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[Foo])

  def getTodos =
    Ajax
      .get(
        url = s"$baseUrl/todos",
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[Vector[TodoTask]])

  def postTodo(newTodo: TodoTask) =
    Ajax
      .post(
        url = s"$baseUrl/todos",
        data = newTodo.asJson.noSpaces,
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[String])

  def switchStatus(todoId: String) =
    Ajax
      .get(
        url = s"$baseUrl/todos/$todoId/switch",
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[TodoStatus])

  def deleteTodo(todoId: String) =
    Ajax
      .delete(
        url = s"$baseUrl/todos/$todoId",
        headers = JSON_TYPE
      )
      .transform(_.responseText, onFailure)

  def getScoreboard =
    Ajax
      .get(
        url = s"$baseUrl/scoreboard",
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[Vector[ScoreboardRecord]])

  def postScore(newScore: ScoreboardRecord) =
    Ajax
      .post(
        url = s"$baseUrl/scoreboard",
        data = newScore.asJson.noSpaces,
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[ScoreboardRecord])

  def deleteAllScores() =
    Ajax
      .delete(
        url = s"$baseUrl/scoreboard",
        headers = JSON_TYPE
      )
      .transform(_.responseText, onFailure)

  def postAuth(cred: AuthCredentials) =
    Ajax
      .post(
        url = s"$baseUrl/auth",
        data = cred.asJson.noSpaces,
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[User])

  def postAuthUser(cred: AuthCredentials) =
    Ajax
      .post(
        url = s"$baseUrl/auth/user",
        data = cred.asJson.noSpaces,
        headers = JSON_TYPE
      )
      .transform(decodeAndHandleErrors[User])

  def getAuthSecured(token: String) =
    Ajax
      .get(
        url = s"$baseUrl/auth/secured",
        headers = JSON_TYPE ++ authHeader(token)
      )
      .transform(decodeAndHandleErrors[String])

  private[this] def decodeAndHandleErrors[A: Decoder](t: Try[XMLHttpRequest]): Try[A] = t match {
    case Success(req) => decode[A](req.responseText).toTry
    case Failure(e)   => Failure(onFailure(e))
  }

  private[this] def onFailure: Throwable => Throwable = t => {
    t.printStackTrace()
    t match {
      case ex: AjaxException =>
        val msgEx: Exception =
          decode[GenericMsgException](ex.xhr.responseText).getOrElse(AjaxErrorException(ex.xhr.responseText))
        ex.xhr.status match {
          case 401 | 409 => msgEx
          case _         => AjaxClient.AjaxErrorException(s"Connection error.")
        }
      case unknown => AjaxClient.UnknownErrorException
    }
  }

  case object UnknownErrorException           extends Exception("unknown error")
  case class AjaxErrorException(s: String)    extends Exception(s)
  case class GenericMsgException(msg: String) extends Exception(msg)
}

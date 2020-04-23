package example.model

object Errors {
  sealed trait ErrorInfo extends Product with Serializable
  case class NotFound(msg: String) extends ErrorInfo
  case class BadRequest(msg: String) extends ErrorInfo
  case class UnknownError(msg: String) extends ErrorInfo
  case class Unauthorized(msg: String) extends ErrorInfo
  case class Conflict(msg: String) extends ErrorInfo

  case class TodoTaskNotFound(msg: String) extends Exception(msg)
  case class WrongMongoId(msg: String) extends Exception(msg)

  case class TokenNotFound(msg: String) extends Exception(msg)
  case class UserExists(msg: String) extends Exception(msg)
  case class AuthenticationError(msg: String) extends Exception(msg)
}

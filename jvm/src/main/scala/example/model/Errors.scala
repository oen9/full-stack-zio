package example.model

object Errors {
  sealed trait ErrorInfo
  case class NotFound(msg: String) extends ErrorInfo
  case class BadRequest(msg: String) extends ErrorInfo
  case class UnknownError(msg: String) extends ErrorInfo

  case class TodoTaskNotFound(msg: String) extends Exception(msg)
  case class WrongMongoId(msg: String) extends Exception(msg)
}

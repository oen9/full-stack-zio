package example.model

object SqlData {
  case class User(
    id: Option[Long] = None,
    name: String,
    password: String,
    token: String
  )
}

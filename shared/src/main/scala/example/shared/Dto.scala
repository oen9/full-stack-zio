package example.shared

object Dto {
    sealed trait Event
    case class Foo(i: Int) extends Event
    case class Bar(s: String) extends Event
    case class Baz(c: Char) extends Event
    case class Qux(values: List[String]) extends Event

    sealed trait TodoStatus
    case object Done extends TodoStatus
    case object Pending extends TodoStatus
    case class TodoTask(id: Option[String] = None, value: String = "todo value", status: TodoStatus = Pending)

    case class ScoreboardRecord(id: Option[Long] = None, name: String = "foo", score: Int = 0)

    case class AuthCredentials(name: String, password: String)
    type Token = String
    case class User(id: Long, name: String, token: Token)

    import io.circe.generic.extras.Configuration
    implicit val circeConfig = Configuration.default.withDiscriminator("eventType").withDefaults
}

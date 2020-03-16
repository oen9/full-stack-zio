package example.shared

object Dto {
    sealed trait Event
    case class Foo(i: Int) extends Event
    case class Bar(s: String) extends Event
    case class Baz(c: Char) extends Event
    case class Qux(values: List[String]) extends Event

    import io.circe.generic.extras.Configuration
    implicit val circeConfig = Configuration.default.withDiscriminator("eventType").withDefaults
}

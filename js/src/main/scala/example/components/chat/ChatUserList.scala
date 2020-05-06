package example.components.chat

import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object ChatUserList {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (users, setUsers) = useState(
      Vector[String]("foo", "bar", "baz")
    )

    div(
      className := "vh-50 overflow-auto mb-3 bg-light",
      users.zipWithIndex.map {
        case (msg, idx) =>
          div(key := idx.toString, className := "alert alert-secondary", msg)
      }
    )
  }
}

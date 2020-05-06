package example.components.chat

import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object ChatView {
  case class Props(msgs: Vector[String] = Vector(), autoscroll: Boolean = true)

  val component = FunctionalComponent[Props] { props =>
    val chatRef = React.createRef[html.Div]

    useLayoutEffect(
      () =>
        if (props.autoscroll) {
          val chatDiv = chatRef.current
          chatDiv.scrollTop = chatDiv.scrollHeight
        },
      Seq(props.msgs, props.autoscroll)
    )

    div(
      className := "vh-50 overflow-auto mb-3 bg-light",
      ref := chatRef,
      props.msgs.zipWithIndex.map {
        case (msg, idx) =>
          div(key := idx.toString, className := "alert alert-success", msg)
      }
    )
  }
}

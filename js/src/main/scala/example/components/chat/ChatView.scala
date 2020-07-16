package example.components.chat

import example.services.AppCircuit
import example.services.ReactDiode
import org.scalajs.dom.html
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.web.html._
import example.shared.Dto

@react object ChatView {
  case class Props(autoscroll: Boolean = true)

  val component = FunctionalComponent[Props] { props =>
    val (msgs, _) = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.msgs))
    val (me, _)   = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.user))

    val chatRef = React.createRef[html.Div]

    useLayoutEffect(
      () =>
        if (props.autoscroll) {
          val chatDiv = chatRef.current
          chatDiv.scrollTop = chatDiv.scrollHeight
        },
      Seq(msgs, props.autoscroll)
    )

    def prettyMsg(m: Dto.ChatMsg) = {
      val color = m match {
        case Dto.ChatMsg(Some(u), msg) if u.id == me.id => "primary"
        case Dto.ChatMsg(None, msg)                     => "warning"
        case _                                          => "secondary"
      }

      val formattedMsg = m match {
        case Dto.ChatMsg(Some(u), msg) =>
          div(
            span(u.name),
            span(className := s"ml-1 badge badge-$color", u.id),
            span(className := s"ml-1", msg)
          )
        case Dto.ChatMsg(None, msg) => div(msg)
      }

      div(
        className := s"alert alert-$color",
        formattedMsg
      )
    }

    div(
      className := "vh-50 overflow-auto mb-3 bg-light",
      ref := chatRef,
      msgs.zipWithIndex.map {
        case (msg, idx) =>
          div(key := idx.toString, prettyMsg(msg))
      }
    )
  }
}

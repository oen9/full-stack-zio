package example.modules

import cats.implicits._
import example.components.chat.ChatUserList
import example.components.chat.ChatView
import example.services.AppCircuit
import example.services.ChatWebsock
import example.services.Connect
import example.services.Disconnect
import example.services.ReactDiode
import example.shared.Dto
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object Chat {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (username, setUsername)     = useState("unknown (you'll be able to change it soon)")
    val (newMsg, setNewMsg)         = useState("")
    val (errors, setErrors)         = useState(Vector[String]())
    val (autoscroll, setAutoscroll) = useState(true)
    val (maybeWs, dispatch)         = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.ws))
    val (msgs, _)                   = ReactDiode.useDiode(AppCircuit.zoomTo(_.chatConn.msgs))

    def handleUsername(e: SyntheticEvent[html.Input, Event]): Unit      = setUsername(e.target.value)
    def handleNewMsg(e: SyntheticEvent[html.Input, Event]): Unit        = setNewMsg(e.target.value)
    def handleSetAutoscroll(e: SyntheticEvent[html.Input, Event]): Unit = setAutoscroll(e.currentTarget.checked)

    def handleSend(e: SyntheticEvent[html.Form, Event]): Unit = {
      e.preventDefault()
      if (newMsg.nonEmpty) {
        maybeWs.fold(())(ws => ChatWebsock.send(ws, Dto.ChatMsg(msg = newMsg)))
        setNewMsg("")
      }
    }

    val connect    = () => dispatch(Connect)
    val disconnect = () => dispatch(Disconnect)

    def chatForm() = Fragment(
      form(
        onSubmit := (handleSend(_)),
        div(
          className := "input-group mb-3",
          div(
            className := "input-group-prepend",
            span(className := "input-group-text", id := "form-username-label", "Username:")
          ),
          input(
            `type` := "text",
            className := "form-control",
            placeholder := "Username",
            aria - "label" := "Username",
            aria - "describedby" := "form-username-label",
            value := username,
            disabled, // TODO
            onChange := (handleUsername(_))
          ),
          div(
            className := "input-group-append",
            label(
              id := "form-autoscroll-label",
              className := "input-group-text",
              htmlFor := "autoscroll-checkbox",
              "autoscroll:"
            ),
            div(
              className := "input-group-text",
              input(
                `type` := "checkbox",
                id := "autoscroll-checkbox",
                className := "ml-2",
                aria - "label" := "autoscroll",
                aria - "describedby" := "form-autoscroll-label",
                checked := autoscroll,
                onChange := (handleSetAutoscroll(_))
              )
            )
          )
        ),
        div(
          className := "row",
          div(className := "col-12 order-2 col-sm-8 order-sm-1", ChatView(autoscroll), autoscroll),
          div(className := "col-12 order-1 col-sm-4 order-sm-2", ChatUserList())
        ),
        div(
          className := "input-group mb-3",
          div(
            className := "input-group-prepend",
            span(className := "input-group-text", "Message:", id := "form-message-label")
          ),
          input(
            `type` := "text",
            className := "form-control",
            placeholder := "Message",
            aria - "label" := "Message",
            aria - "describedby" := "form-message-label",
            value := newMsg,
            onChange := (handleNewMsg(_))
          )
        ),
        errors.zipWithIndex.map {
          case (msg, idx) =>
            div(key := idx.toString, className := "alert alert-danger", role := "alert", msg)
        },
        div(
          className := "row",
          div(className := "col", button(`type` := "submit", className := "btn btn-secondary w-100", "send"))
        )
      )
    )

    div(
      className := "card",
      div(
        className := "card-header",
        div(
          className := "row",
          div(className := "col", div("Chat")),
          div(
            className := "col",
            div(
              className := "text-right",
              button(className := "btn btn-primary", disabled := maybeWs.isDefined, onClick := connect, "connect"),
              button(className := "btn btn-danger", disabled := maybeWs.isEmpty, onClick := disconnect, "disconnect")
            )
          )
        )
      ),
      div(
        className := "card-body",
        h5(className := "card-title", "Open in new tab/window to test (this is in progress)"),
        chatForm())
    )
  }
}

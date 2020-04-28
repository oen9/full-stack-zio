package example.modules

import cats.implicits._
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.facade.Hooks._
import slinky.core.facade.React
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object Chat {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (username, setUsername) = useState("unknown")
    val (newMsg, setNewMsg) = useState("")
    val (errors, setErrors) = useState(Vector[String]())
    val (msgs, setMsgs) = useState(Vector[String]())
    val (autoscroll, setAutoscroll) = useState(true)
    val chatRef = React.createRef[html.Div]

    def handleUsername(e: SyntheticEvent[html.Input, Event]): Unit = setUsername(e.target.value)
    def handleNewMsg(e: SyntheticEvent[html.Input, Event]): Unit = setNewMsg(e.target.value)
    def handleSetAutoscroll(e: SyntheticEvent[html.Input, Event]): Unit = setAutoscroll(e.currentTarget.checked)

    def handleSend(e: SyntheticEvent[html.Form, Event]): Unit = {
      e.preventDefault()
      if (newMsg.nonEmpty) {
        setMsgs(msgs :+ s"$username: $newMsg")
        setNewMsg("")
      }
    }

    useLayoutEffect(() => {
      if (autoscroll) {
        val chatDiv = chatRef.current
        chatDiv.scrollTop = chatDiv.scrollHeight
      }
    }, Seq(msgs, autoscroll))

    def chatForm() = Fragment(
      form(onSubmit := (handleSend(_)),
        div(className := "input-group mb-3",
          div(className := "input-group-prepend",
            span(className := "input-group-text", id := "form-username-label", "username:")
          ),
          input(`type` := "text",
            className := "form-control",
            placeholder := "Username",
            aria-"label" := "Username",
            aria-"describedby" := "form-username-label",
            value := username,
            onChange := (handleUsername(_))
          ),
          div(className := "input-group-append",
            label(id := "form-autoscroll-label",
              className := "input-group-text",
              htmlFor := "autoscroll-checkbox",
              "autoscroll:"
            ),
            div(className := "input-group-text",
              input(`type` := "checkbox",
                id := "autoscroll-checkbox",
                className := "ml-2",
                aria-"label" := "autoscroll",
                aria-"describedby" := "form-autoscroll-label",
                checked := autoscroll,
                onChange := (handleSetAutoscroll(_))
              )
            )
          ),
        ),
        div(className := "vh-50 overflow-auto mb-3", ref := chatRef,
          msgs.zipWithIndex.map { case (msg, idx) =>
            div(key := idx.toString,className := "alert alert-success", role := "alert", msg)
          },
        ),
        div(className := "input-group mb-3",
          div(className := "input-group-prepend",
            span(className := "input-group-text", "message", id := "form-message-label")
          ),
          input(`type` := "text",
            className := "form-control",
            placeholder := "Message",
            aria-"label" := "Message",
            aria-"describedby" := "form-message-label",
            value := newMsg,
            onChange := (handleNewMsg(_))
          )
        ),
        errors.zipWithIndex.map { case (msg, idx) =>
          div(key := idx.toString,className := "alert alert-danger", role := "alert", msg)
        },
        div(className := "row",
          div(className := "col",
            button(`type` := "submit", className := "btn btn-secondary w-100", "send"),
          ),
        )
      )
    )

    div(className := "card",
      div(className := "card-header", "Chat"),
      div(className := "card-body",
        h5(className := "card-title",
          "Nothing here. Everything in progress."
        ),
        chatForm()
      )
    )
  }
}

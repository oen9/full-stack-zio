package example.components

import cats.implicits._
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._
import example.services.ReactDiode
import example.services.AppCircuit
import example.services.SetGlobalName

@react object GlobalName {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (name, setName)        = useState("unknown")
    val (globalName, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.globalName))

    useEffect(() => setName(globalName), Seq())

    def onChangeName(e: SyntheticEvent[html.Input, Event]): Unit = setName(e.target.value)

    def handleSend(e: SyntheticEvent[html.Form, Event]): Unit = {
      e.preventDefault()
      if (name.trim.nonEmpty)
        dispatch(SetGlobalName(name))
      else
        setName(globalName)
    }

    form(
      onSubmit := (handleSend(_)),
      div(
        className := "input-group mb-1",
        div(className := "input-group-prepend", span(className := "input-group-text", "Username:")),
        input(className := "form-control", value := name, onChange := (onChangeName(_))),
        div(
          className := "input-group-append",
          button(
            `type` := "submit",
            className := "btn btn-success",
            disabled := name == globalName,
            "accept"
          )
        )
      )
    )
  }
}

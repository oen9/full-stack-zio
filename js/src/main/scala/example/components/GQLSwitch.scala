package example.components

import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.SetStateHookCallback
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object GQLSwitch {
  case class Props(checked: Boolean, sshc: SetStateHookCallback[Boolean], text: String)

  val component = FunctionalComponent[Props] { props =>
    def handleSwitch(sshc: SetStateHookCallback[Boolean])(e: SyntheticEvent[html.Input, Event]): Unit =
      sshc(e.currentTarget.checked)

    div(
      className := "custom-control custom-switch",
      input(
        `type` := "checkbox",
        className := "custom-control-input",
        id := props.text,
        checked := props.checked,
        onChange := (handleSwitch(props.sshc)(_))
      ),
      label(className := "custom-control-label", htmlFor := props.text, props.text)
    )
  }
}

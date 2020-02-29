package example.components

import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object BlueButton {
  case class Props(text: String, onClick: () => Unit)
  val component = FunctionalComponent[Props] { props =>
    button(className := "btn btn-primary", onClick := props.onClick, props.text)
  }
}

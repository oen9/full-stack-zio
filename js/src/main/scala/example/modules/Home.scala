package example.modules

import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object Home {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    div(className := "card",
      div(className := "card-header", "Home"),
      div(className := "card-body",
        h5(className := "card-title text-center",
          "Hello!"
        )
      )
    )
  }
}

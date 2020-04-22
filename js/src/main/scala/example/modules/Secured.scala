package example.modules
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object Secured {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    div(className := "card",
      div(className := "card-header", "Secured"),
      div(className := "card-body",
        "This is some secret text available only after signing in."
      )
    )
  }
}

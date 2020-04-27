package example.components

import cats.implicits._
import diode.data.PotState.PotFailed
import example.services.AppCircuit
import example.services.ReactDiode
import example.services.SignOut
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object AuthLastError {
  type Props = Unit

  val component = FunctionalComponent[Props] { props =>
    val (auth, dispatch) = ReactDiode.useDiode(AppCircuit.zoom(_.auth))
    val clean = () => dispatch(SignOut)

    def lastErrorMsg() = auth
      .exceptionOption
      .fold("unknown error")(msg =>
        s"Last auth error: ${msg.getMessage()}"
      )

    auth.state match {
      case PotFailed =>
        div(className := "alert alert-danger", role := "alert",
          div(className := "row align-items-center",
            div(className := "col", lastErrorMsg()),
            div(className := "col text-right",
              button(className := "btn btn-secondary text-right", "clean", onClick := clean)
            ),
          )
        ).some
      case _ => none[ReactElement]
    }
  }
}

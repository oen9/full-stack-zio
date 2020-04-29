package example.modules

import cats.implicits._
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import example.services.AppCircuit
import example.services.ReactDiode
import example.services.TryGetSecuredText
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object Secured {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (auth, dispatch) = ReactDiode.useDiode(AppCircuit.zoom(_.auth))
    val (secretText, _)  = ReactDiode.useDiode(AppCircuit.zoom(_.securedText))

    useEffect(
      () =>
        auth.state match {
          case PotReady => dispatch(TryGetSecuredText(auth.get.token))
          case _        => ()
        },
      Seq()
    )

    div(
      className := "card",
      div(className := "card-header", "Secured"),
      div(
        className := "card-body",
        div("This page is available only after signing in."),
        div(
          secretText.state match {
            case PotPending =>
              div(
                className := "spinner-border text-primary",
                role := "status",
                span(className := "sr-only", "Loading...")
              ).some
            case PotFailed =>
              div(
                secretText.exceptionOption
                  .fold("unknown error")(msg => s"error: ${msg.getMessage()}")
              )
            case PotReady =>
              secretText.fold("unknown error")(s => s"Secured text: $s").some
            case _ => none[ReactElement]
          }
        )
      )
    )
  }
}

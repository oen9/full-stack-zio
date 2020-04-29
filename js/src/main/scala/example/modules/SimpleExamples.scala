package example.modules

import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.FunctionalComponent
import slinky.web.html._

import example.components.BlueButton
import example.services.AppCircuit
import example.services.IncreaseClicks
import example.services.ReactDiode
import example.services.TryGetRandom
import example.shared.HelloShared

@react object SimpleExamples {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    val (clicks, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.clicks))
    val (randomNumber, _)  = ReactDiode.useDiode(AppCircuit.zoomTo(_.randomNumber))

    Fragment(
      div(className := "text-center", "compile-time shared string between js and jvm: " + HelloShared.TEST_STR),
      div(
        className := "row mt-2",
        div(className := "col text-right", BlueButton("more clicks", () => dispatch(IncreaseClicks))),
        div(className := "col", " clicks: " + clicks.count)
      ),
      div(
        className := "row mt-2",
        div(className := "col text-right", BlueButton("new random", () => dispatch(TryGetRandom()))),
        div(
          className := "col",
          " random: ",
          randomNumber.state match {
            case PotEmpty =>
              div("nothing here")
            case PotPending =>
              div(
                className := "spinner-border text-primary",
                role := "status",
                span(className := "sr-only", "Loading...")
              )
            case PotFailed =>
              randomNumber.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
            case PotReady =>
              randomNumber.fold("unknown error")(_.i.toString)
            case _ => div()
          }
        )
      )
    )
  }
}

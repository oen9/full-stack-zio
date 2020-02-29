package example.modules

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html._
//import example.services.ReactDiode
//import example.services.AppCircuit
//import example.services.IncreaseClicks
//import example.services.TryGetRandom
//import diode.data.PotState.PotEmpty
//import diode.data.PotState.PotPending
//import diode.data.PotState.PotFailed
//import diode.data.PotState.PotReady
import slinky.core.facade.Fragment
import example.shared.HelloShared
import example.components.BlueButton

@react object Home {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    //val (clicks, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.clicks))
    //val (randomNumber, _) = ReactDiode.useDiode(AppCircuit.zoomTo(_.randomNumber))

    Fragment(
      div(className := "text-center",
        "Hello: " + HelloShared.TEST_STR
      ),
      div(className := "row mt-2",
        div(className := "col text-right",
      //    BlueButton("click me!", () => dispatch(IncreaseClicks))
          BlueButton("dummy button", () => ())
        ),
      //  div(className := "col", " clicks: " + clicks.count)
        div(className := "col", " clicks: " + 66)
      ),
      div(className := "row mt-2",
        div(className := "col text-right",
      //    BlueButton("get random from server!", () => dispatch(TryGetRandom()))
          BlueButton("get random from server! (dummy)", () => ())
        ),
        div(className := "col", " random: ",
            div("nothing here")
      //    randomNumber.state match {
      //      case PotEmpty =>
      //        div("nothing here")
      //      case PotPending =>
      //        div(className := "spinner-border text-primary", role := "status",
      //          span(className := "sr-only", "Loading...")
      //        )
      //      case PotFailed =>
      //        randomNumber.exceptionOption.fold("unknown error")(msg => " error: " + msg.toString)
      //      case PotReady =>
      //        randomNumber.fold("unknown error")(_.i.toString)
      //      case _ => div()
      //    }
        )
      )
    )
  }
}

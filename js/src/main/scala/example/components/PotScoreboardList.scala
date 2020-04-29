package example.components

import diode.data.Pot
import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import example.shared.Dto.ScoreboardRecord
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.web.html._
import slinky.core.facade.ReactElement

@react object PotScoreboardList {
  case class Props(scores: Pot[Vector[ScoreboardRecord]])

  val component = FunctionalComponent[Props] { props =>
    props.scores.state match {
      case PotEmpty =>
        "nothing here"
      case PotPending =>
        div(
          className := "row justify-content-center",
          div(
            className := "row justify-content-center spinner-border text-primary",
            role := "status",
            span(className := "sr-only", "Loading...")
          )
        )
      case PotFailed =>
        props.scores.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
      case PotReady =>
        props.scores.fold(
          div("nothing here yet"): ReactElement
        )(scoreList => ul(className := "list-group list-group-flush", ScoreboardList(scoreList)))
      case _ => div("unexpected state")
    }
  }
}

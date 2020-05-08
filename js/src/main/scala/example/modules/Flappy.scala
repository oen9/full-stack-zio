package example.modules

import example.components.DeleteDialog
import example.components.GlobalName
import example.components.PotScoreboardList
import example.modules.flappybird.FlappyBird
import example.services.AddNewScore
import example.services.AppCircuit
import example.services.ClearScoreboard
import example.services.ReactDiode
import example.services.TryGetScoreboard
import example.shared.Dto.ScoreboardRecord
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object Flappy {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    val (scores, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.scores))
    val (score, setScore)  = useState(0)
    val (name, _)          = ReactDiode.useDiode(AppCircuit.zoomTo(_.globalName))

    useEffect(() => dispatch(TryGetScoreboard()), Seq())

    val submitDeleteAll = () => dispatch(ClearScoreboard)
    def addNewScoreboardRecord(score: Int): Unit =
      dispatch(AddNewScore(ScoreboardRecord(name = name, score = score)))

    Fragment(
      DeleteDialog(
        onDelete = submitDeleteAll,
        content = div("Are you sure you want to delete all record?!")
      ),
      div(
        className := "row justify-content-center",
        div(
          className := "scoreboard-size mb-2",
          GlobalName()
        ),
        FlappyBird(setScore = addNewScoreboardRecord),
        div(
          className := "card scoreboard-size mt-2",
          div(className := "card-header", "scoreboard"),
          div(
            className := "card-body",
            PotScoreboardList(scores),
            div(
              className := "row",
              button(
                className := "btn btn-danger w-100",
                data - "toggle" := "modal",
                data - "target" := "#deleteModal",
                "delete all saved scores",
                i(className := "ml-2 fas fa-trash")
              )
            )
          )
        )
      )
    )
  }
}

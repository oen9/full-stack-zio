package example.modules

import example.components.DeleteDialog
import example.components.PotScoreboardList
import example.services.AppCircuit
import example.services.ClearScoreboard
import example.services.ReactDiode
import example.services.TryGetScoreboard

import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._
import example.modules.flappybird.FlappyBird
import example.services.AddNewScore
import example.shared.Dto.ScoreboardRecord

@react object Flappy {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    val (scores, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.scores))
    val (score, setScore) = useState(0)
    val (name, setName) = useState("unknown")

    useEffect(() => {
      dispatch(TryGetScoreboard())
    }, Seq())

    val submitDeleteAll = () => dispatch(ClearScoreboard)
    def onChangeName(e: SyntheticEvent[html.Input, Event]): Unit = setName(e.target.value)
    def addNewScoreboardRecord(score: Int): Unit = {
      dispatch(AddNewScore(ScoreboardRecord(name = name, score = score)))
    }

    Fragment(
      DeleteDialog(
        onDelete = submitDeleteAll,
        content = div("Are you sure you want to delete all record?!")
      ),

      div(className := "row justify-content-center",
        div(className := "scoreboard-size mb-2",
          div(className := "input-group",
            div(className := "input-group-prepend",
              span(className := "input-group-text", "your name on scoreboard:"),
            ),
            input(className := "form-control width-100", value := name, onChange := (onChangeName(_))),
          )
        ),

        FlappyBird(setScore = addNewScoreboardRecord),

        div(className := "card scoreboard-size mt-2",
          div(className := "card-header",
            "scoreboard"
          ),
          div(className := "card-body",
            PotScoreboardList(scores),
            div(className := "row",
              button(className := "btn btn-danger w-100", data-"toggle" := "modal", data-"target" := "#deleteModal",
                "delete all saved scores",
                i(className := "ml-2 fas fa-trash")
              ),
            ),
          )
        )
      )
    )
  }
}

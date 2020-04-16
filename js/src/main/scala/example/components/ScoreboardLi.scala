package example.components

import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.FunctionalComponent
import example.shared.Dto.ScoreboardRecord

@react object ScoreboardLi {
  case class Props(score: ScoreboardRecord, pos: Int)

  val component = FunctionalComponent[Props] { props =>
    li(className := "list-group-item",
      div(className := "row",
        div(className := "col text-center", s"${props.score.score}"),
        div(className := "col text-center", s"${props.score.name}"),
        div(className := "col text-center", 
          props.pos match {
            case 0 => i(className := "fas fa-trophy gold")
            case 1 => i(className := "fas fa-trophy silver")
            case 2 => i(className := "fas fa-trophy brown")
            case _ => i(className := "fas fa-kiwi-bird green")
          }
        ),
      )
    )
  }

}

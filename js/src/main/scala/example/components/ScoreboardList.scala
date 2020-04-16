package example.components

import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.FunctionalComponent
import example.shared.Dto.ScoreboardRecord

@react object ScoreboardList {
  case class Props(scores: Vector[ScoreboardRecord])

  val component = FunctionalComponent[Props] { props =>
    ul(className := "list-group list-group-flush",
      li(className := "list-group-item",
        div(className := "row",
          div(className := "col text-center", "score"),
          div(className := "col text-center", "name"),
          div(className := "col text-center", "trophy"),
        )
      ),
      div(className := "overflow-auto max-vh-50",
        props.scores.zipWithIndex.map {
          case (s, pos) => ScoreboardLi(s, pos).withKey(s.id.getOrElse(-1).toString())
        }
      )
    )
  }
}

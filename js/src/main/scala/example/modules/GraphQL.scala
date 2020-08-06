package example.modules

import example.components.graphql.ItemDetails
import example.components.graphql.ItemsList
import example.services.AppCircuit
import example.services.GetGQLItems
import org.scalajs.dom.{html, Event}
import slinky.core.annotations.react
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object GraphQL {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    def handleRefresh(e: SyntheticEvent[html.Form, Event]): Unit = {
      e.preventDefault()
      AppCircuit.dispatch(GetGQLItems())
    }

    div(
      className := "card",
      div(
        className := "card-header",
        div(
          className := "row",
          div(className := "col", div("GraphQL showcase"))
        )
      ),
      div(
        className := "card-body",
        form(
          className := "mb-2",
          onSubmit := (handleRefresh(_)),
          button(`type` := "submit", className := "btn btn-success", "refresh data")
        ),
        div(
          className := "row",
          div(
            className := "col-md",
            ItemsList()
          ),
          div(
            className := "col-md",
            ItemDetails()
          )
        )
      )
    )
  }
}

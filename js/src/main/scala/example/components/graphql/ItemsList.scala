package example.components.graphql

import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import example.services.AppCircuit
import example.services.GetGQLItem
import example.services.GraphQLClient.ItemBaseView
import example.services.ReactDiode
import org.scalajs.dom.html
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.web.html._
import slinky.web.SyntheticMouseEvent

@react object ItemsList {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (items, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.graphQLItems.items))

    def selectItem(name: String)(e: SyntheticMouseEvent[html.Button]): Unit = {
      e.preventDefault()
      println(s"selected: $name")
      dispatch(GetGQLItem(name = name))
    }

    def prettyItems(items: List[ItemBaseView]) =
      ul(
        className := "list-group",
        items.map { item =>
          li(
            key := item.name,
            className := "list-group-item",
            div(
              className := "row align-items-center",
              div(className := "col-sm", item.name),
              div(
                className := "col-sm text-right",
                button(
                  `type` := "button",
                  className := "btn btn-primary",
                  onClick := (selectItem(item.name)(_)),
                  "show full",
                  i(className := "ml-2 fas fa-chevron-circle-right")
                )
              )
            )
          )
        }
      )

    items.state match {
      case PotEmpty =>
        "nothing here (click refresh button?)"
      case PotPending =>
        div(
          className := "row justify-content-center",
          div(
            className := "spinner-border text-primary",
            role := "status",
            span(className := "sr-only", "Loading...")
          )
        )
      case PotFailed =>
        items.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
      case PotReady =>
        items.fold(
          div("nothing here (click refresh button?)"): ReactElement
        )(prettyItems)
      case _ => div("unexpected state")
    }
  }
}

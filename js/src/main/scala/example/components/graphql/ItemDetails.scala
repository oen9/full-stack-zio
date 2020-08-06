package example.components.graphql

import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import example.services.AppCircuit
import example.services.GraphQLClient.ItemFullView
import example.services.ReactDiode
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.web.html._

@react object ItemDetails {
  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val (item, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.graphQLItems.selectedItem))

    def prettyMaybeItem(maybeItem: Option[ItemFullView]) =
      maybeItem.fold(
        div("Sorry but this item doesn't exist")
      )(prettyItem)

    def prettyItem(item: ItemFullView) =
      div(
        className := "card",
        div(
          className := "card-header",
          div(
            className := "row",
            div(className := "col", div(s"${item.name} details"))
          )
        ),
        div(
          className := "card-body",
          h5(className := "card-title", s"Amount: ${item.amount}"),
          div(
            className := "row",
            div(className := "col", b("name")),
            div(className := "col", b("value")),
            div(className := "col", b("description"))
          ),
          hr(),
          item.features.map { feat =>
            div(
              key := feat.name,
              className := "row",
              div(className := "col", feat.name),
              div(className := "col", feat.value),
              div(className := "col", feat.description)
            )
          }
        )
      )

    item.state match {
      case PotEmpty =>
        "First refresh data and then select item"
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
        item.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
      case PotReady =>
        item.fold(
          div("selected item doesn't exist"): ReactElement
        )(prettyMaybeItem)
      case _ => div("unexpected state")
    }
  }
}

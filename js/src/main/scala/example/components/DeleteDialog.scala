package example.components

import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.FunctionalComponent
import slinky.core.facade.ReactElement

@react object DeleteDialog {
  case class Props(
    onDelete: () => Unit = () => (),
    onCancel: () => Unit = () => (),
    content: ReactElement = div()
  )

  val component = FunctionalComponent[Props] { props =>
    div(
      className := "modal fade",
      id := "deleteModal",
      data - "backdrop" := "static",
      tabIndex := -1,
      role := "dialog",
      aria - "labelledby" := "deleteModalCenterTitle",
      aria - "hidden" := "true",
      div(
        className := "modal-dialog modal-dialog-centered",
        role := "document",
        div(
          className := "modal-content",
          div(
            className := "modal-header",
            h5(className := "modal-title", id := "deleteModalCenterTitle", s"Delete"),
            button(
              `type` := "button",
              className := "close",
              data - "dismiss" := "modal",
              aria - "label" := "Close",
              span(aria - "hidden" := "true", "×", onClick := props.onCancel)
            )
          ),
          div(className := "modal-body", props.content),
          div(
            className := "modal-footer",
            button(
              `type` := "button",
              className := "btn btn-secondary",
              data - "dismiss" := "modal",
              onClick := props.onCancel,
              "cancel"
            ),
            button(
              `type` := "button",
              className := "btn btn-danger",
              data - "dismiss" := "modal",
              "delete",
              onClick := props.onDelete
            )
          )
        )
      )
    )
  }
}

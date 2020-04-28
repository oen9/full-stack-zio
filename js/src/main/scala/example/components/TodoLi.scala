package example.components

import slinky.core.annotations.react
import slinky.web.html._
import slinky.core.FunctionalComponent
import example.shared.Dto.TodoTask
import example.shared.Dto.Done
import example.shared.Dto.Pending
import example.services.ReactDiode
import example.services.AppCircuit
import example.services.SwitchTodoStatus

@react object TodoLi {
  case class Props(todoTask: TodoTask, onDelete: () => Unit)

  val component = FunctionalComponent[Props] { props =>
    val (_, dispatch) = ReactDiode.useDiode(AppCircuit.zoom(identity))

    val onSwitchStatus = () => dispatch(SwitchTodoStatus(props.todoTask.id.getOrElse("")))

    li(className := "list-group-item",
      div(className := "row align-items-center",
        div(className := "col-sm col-md-8",
          props.todoTask.status match {
            case Done => s(props.todoTask.value)
            case Pending => props.todoTask.value
          }
        ),
        div(className := "col-sm col-md-4 text-right",
          props.todoTask.status match {
            case Done => button(className := "btn btn-warning", i(className := "fas fa-backspace"), onClick := onSwitchStatus)
            case Pending => button(className := "btn btn-success", i(className := "fas fa-check-circle"), onClick := onSwitchStatus)
          },
          button(className := "btn btn-danger", data-"toggle" := "modal", data-"target" := "#deleteModal", onClick := props.onDelete,
            i(className := "fas fa-trash")
          )
        )
      )
    ),
  }

}

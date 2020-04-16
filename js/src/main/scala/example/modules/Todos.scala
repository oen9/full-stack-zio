package example.modules

import cats.implicits._

import example.components.DeleteDialog
import example.components.TodoLi
import example.services.AddNewTodo
import example.services.AppCircuit
import example.services.DeleteTodo
import example.services.ReactDiode
import example.services.TryGetTodos
import example.shared.Dto.TodoTask

import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactElement
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.web.html._

@react object Todos {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    val (todos, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.todos))
    val (toDelete, setToDelete) = useState(none[TodoTask])
    val (toAdd, setToAdd) = useState("")

    useEffect(() => {
      dispatch(TryGetTodos())
    }, Seq())

    val clearToDelete = () => setToDelete(none)
    val submitDelete = () => dispatch(DeleteTodo(toDelete.flatMap(_.id).getOrElse("")))
    def onDelete(todoTask: TodoTask) = () => setToDelete(todoTask.some)
    def onChangeAddNew(e: SyntheticEvent[html.Input, Event]): Unit = setToAdd(e.target.value)
    def onClickAddNew(e: SyntheticEvent[html.Button, Event]): Unit = {
      e.preventDefault()
      if (toAdd.trim().nonEmpty) {
        val todoTask = TodoTask(value = toAdd)
        val action = AddNewTodo(todoTask)
        dispatch(action)
        setToAdd("")
      }
    }

    Fragment(
      DeleteDialog(
        onDelete = submitDelete,
        onCancel = clearToDelete,
        content =
          toDelete.fold(
            div("error: can't find selected item'"): ReactElement
          ) { todo =>
            table(className := "table table-striped",
              tbody(
                tr(
                  td("id"),
                  td(todo.id.fold("unknown")(identity))
                ),
                tr(
                  td("value"),
                  td(todo.value)
                ),
                tr(
                  td("status"),
                  td(todo.status.toString)
                )
              )
            )
          }
      ),

      div(className := "row justify-content-center",
        div(className := "todo-size",
          form(
            div(className := "input-group",
              input(className := "form-control width-100", value := toAdd, onChange := (onChangeAddNew(_))),
              span(className := "input-group-append",
                button(className := "btn btn-primary", onClick := (onClickAddNew(_)),
                  i(className := "fas fa-plus-circle"),
                  " add new"
                ),
              )
            )
          )
        ),

        div(className := "card todo-size mt-2",
          div(className := "card-header",
            "TODO list"
          ),
          div(className := "card-body",
            todos.state match {
              case PotEmpty =>
                "nothing here"
              case PotPending =>
                div(className := "row justify-content-center",
                  div(className := "spinner-border text-primary", role := "status",
                    span(className := "sr-only", "Loading...")
                  )
                )
              case PotFailed =>
                todos.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
              case PotReady =>
                todos.fold(
                  div("nothing here yet"): ReactElement
                )(ts =>
                  ul(className := "list-group list-group-flush",
                    ts.map(t => TodoLi(todoTask = t, onDelete = onDelete(t)).withKey(t.id.getOrElse("error-key")))
                  )
                )
              case _ => div("unexpected state")
            }
          )
        )
      )
    )
  }
}

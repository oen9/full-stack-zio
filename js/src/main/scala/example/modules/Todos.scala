package example.modules

import cats.implicits._

import example.components.TodoLi
import example.services.AppCircuit
import example.services.ReactDiode
import example.services.TryGetTodos
import example.shared.Dto.TodoTask
import example.services.AddNewTodo

import diode.data.PotState.PotEmpty
import diode.data.PotState.PotFailed
import diode.data.PotState.PotPending
import diode.data.PotState.PotReady
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks._
import slinky.core.FunctionalComponent
import slinky.core.SyntheticEvent
import slinky.core.TagElement
import slinky.core.TagMod
import slinky.web.html._

@react object Todos {
  type Props = Unit
  val component = FunctionalComponent[Props] { _ =>
    val (todos, dispatch) = ReactDiode.useDiode(AppCircuit.zoomTo(_.todos))
    val (toDelete, setToDelete) = useState(none[TodoTask])
    val (toAdd, setToAdd) = useState("")

    useEffect(() => {
      todos state match {
        case PotEmpty =>
          dispatch(TryGetTodos())
        case _ =>
      }
    })

    val clearToDelete = () => setToDelete(none)
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
      div(className := "modal fade", id := "deleteModal", data-"backdrop" := "static", tabIndex := -1, role := "dialog", aria-"labelledby" := "deleteModalCenterTitle", aria-"hidden" := "true",
        div(className := "modal-dialog modal-dialog-centered", role := "document",
          div(className := "modal-content",
            div(className := "modal-header",
              h5(className := "modal-title", id := "deleteModalCenterTitle", s"Delete item"),
              button(`type` := "button", className := "close", data-"dismiss" := "modal", aria-"label" := "Close",
                span(aria-"hidden" := "true", "×", onClick := clearToDelete)
              )
            ),
            div(className := "modal-body",
              toDelete.fold(
                div("error: can't find selected item'"): TagMod[TagElement]
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
            div(className := "modal-footer",
              button(`type` := "button", className := "btn btn-secondary", data-"dismiss" := "modal", onClick := clearToDelete, "cancel"),
              button(`type` := "button", className := "btn btn-danger", "delete"),
            )
          )
        )
      ),

      div(className := "row justify-content-center",
        div(className := "todo-size",
          form(
            div(className := "input-group",
              input(className := "form-control add-todo-input", value := toAdd, onChange := (onChangeAddNew(_))),
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
                div(className := "spinner-border text-primary", role := "status",
                  span(className := "sr-only", "Loading...")
                )
              case PotFailed =>
                todos.exceptionOption.fold("unknown error")(msg => " error: " + msg.getMessage())
              case PotReady =>
                todos.fold(
                  div("nothing here yet"): TagMod[TagElement]
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

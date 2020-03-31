package example.services.handlers

import cats.implicits._
import com.softwaremill.quicklens._
import diode.data.Pot
import diode.data.PotAction
import diode.data.Ready
import diode.Effect
import diode.{ActionHandler, ModelRW}
import example.shared.Dto.TodoTask
import example.services.TryGetTodos
import example.services.AddNewTodo
import example.services.TodoAdded
import example.services.SwitchTodoStatus
import example.services.TodoStatusSwitched
import example.services.AjaxClient

class TodosHandler[M](modelRW: ModelRW[M, Pot[Vector[TodoTask]]]) extends ActionHandler(modelRW) {
  import scala.concurrent.ExecutionContext.Implicits.global
  override def handle = {
    case action: TryGetTodos =>
      val updateF = action.effect(AjaxClient.getTodos)(identity _)
      action.handleWith(this, updateF)(PotAction.handler())

    case AddNewTodo(newTodo) =>
      val addEffect = Effect(AjaxClient.postTodo(newTodo).map(newId => TodoAdded(newTodo.copy(id = newId.some))))
      effectOnly(addEffect)
    case TodoAdded(newTodo) =>
      val newValue = value.fold(value)(todos => Ready(todos :+ newTodo))
      updated(newValue)

    case SwitchTodoStatus(idToSwitch) =>
      val addEffect = Effect(AjaxClient.switchStatus(idToSwitch).map(newStatus => TodoStatusSwitched(idToSwitch, newStatus)))
      effectOnly(addEffect)
    case TodoStatusSwitched(id, newStatus) =>
      val idPred = (todo: TodoTask) => todo.id === id.some
      val newValue = value.fold(value)((todos: Vector[TodoTask]) => Ready({
        todos.modify(_.eachWhere(idPred).status).setTo(newStatus)
      }))
      updated(newValue)
  }
}

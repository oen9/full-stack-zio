package example.services.handlers

import cats.implicits._
import com.softwaremill.quicklens._
import diode.data.Pot
import diode.data.PotAction
import diode.data.Ready
import diode.Effect
import diode.{ActionHandler, ModelRW}
import example.services.AddNewTodo
import example.services.AjaxClient
import example.services.DeleteTodo
import example.services.SwitchTodoStatus
import example.services.TodoAdded
import example.services.TodoDeleted
import example.services.TodoStatusSwitched
import example.services.TryGetTodos
import example.shared.Dto.TodoTask

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
      val switchTodoEffect = Effect(AjaxClient.switchStatus(idToSwitch).map(newStatus => TodoStatusSwitched(idToSwitch, newStatus)))
      effectOnly(switchTodoEffect)
    case TodoStatusSwitched(id, newStatus) =>
      val idPred = (todo: TodoTask) => todo.id === id.some
      val newValue = value.fold(value)((todos: Vector[TodoTask]) => Ready({
        todos.modify(_.eachWhere(idPred).status).setTo(newStatus)
      }))
      updated(newValue)

    case DeleteTodo(id) =>
      val deleteEffect = Effect(AjaxClient.deleteTodo(id).map(_ => TodoDeleted(id)))
      effectOnly(deleteEffect)
    case TodoDeleted(id) =>
      val newValue = value.fold(value)((todos: Vector[TodoTask]) => Ready({
        todos.filter(_.id =!= id.some)
      }))
      updated(newValue)
  }
}

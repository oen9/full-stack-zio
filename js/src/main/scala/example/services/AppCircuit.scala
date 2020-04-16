package example.services

import diode.data.Empty
import diode.data.Pot
import diode.data.PotAction
import diode.{Action, Circuit}
import example.services.handlers.ClicksHandler
import example.services.handlers.RandomNumberHandler
import example.services.handlers.TodosHandler
import example.shared.Dto.Foo
import example.shared.Dto.TodoStatus
import example.shared.Dto.TodoTask
import example.shared.Dto.ScoreboardRecord
import example.services.handlers.ScoreboardHandler

case class Clicks(count: Int)
case class RootModel(
  clicks: Clicks,
  randomNumber: Pot[Foo] = Empty,
  todos: Pot[Vector[TodoTask]] = Empty,
  scores: Pot[Vector[ScoreboardRecord]] = Empty,
)

case object IncreaseClicks extends Action
case class TryGetRandom(potResult: Pot[Foo] = Empty) extends PotAction[Foo, TryGetRandom] {
  def next(newResult: Pot[Foo]) = copy(potResult = newResult)
}

case class AddNewTodo(todoTask: TodoTask) extends Action
case class TodoAdded(todoTask: TodoTask) extends Action
case class SwitchTodoStatus(id: String) extends Action
case class TodoStatusSwitched(id: String, newState: TodoStatus) extends Action
case class DeleteTodo(id: String) extends Action
case class TodoDeleted(id: String) extends Action
case class TryGetTodos(potResult: Pot[Vector[TodoTask]] = Empty) extends PotAction[Vector[TodoTask], TryGetTodos] {
  def next(newResult: Pot[Vector[TodoTask]]) = copy(potResult = newResult)
}

case class AddNewScore(score: ScoreboardRecord) extends Action
case class ScoreAdded(score: ScoreboardRecord) extends Action
case object ClearScoreboard extends Action
case object ScoreboardCleared extends Action
case class TryGetScoreboard(potResult: Pot[Vector[ScoreboardRecord]] = Empty) extends PotAction[Vector[ScoreboardRecord], TryGetScoreboard] {
  def next(newResult: Pot[Vector[ScoreboardRecord]]) = copy(potResult = newResult)
}

object AppCircuit extends Circuit[RootModel] {
  override protected def initialModel: RootModel = RootModel(Clicks(0))

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomTo(_.clicks)),
    new RandomNumberHandler(zoomTo(_.randomNumber)),
    new TodosHandler(zoomTo(_.todos)),
    new ScoreboardHandler(zoomTo(_.scores)),
  )
}

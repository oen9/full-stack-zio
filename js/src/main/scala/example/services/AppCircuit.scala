package example.services

import diode.data.Empty
import diode.data.Pot
import diode.data.PotAction
import diode.{Action, Circuit}
import example.services.handlers.AuthHandler
import example.services.handlers.ChatHandler
import example.services.handlers.ClicksHandler
import example.services.handlers.GlobalNameHandler
import example.services.handlers.RandomNumberHandler
import example.services.handlers.ScoreboardHandler
import example.services.handlers.SecuredTextHandler
import example.services.handlers.TodosHandler
import example.services.handlers.WebsockLifecycleHandler
import example.shared.Dto
import example.shared.Dto.Foo
import example.shared.Dto.ScoreboardRecord
import example.shared.Dto.TodoStatus
import example.shared.Dto.TodoTask
import org.scalajs.dom.raw.WebSocket

case class Clicks(count: Int)
case class Auth(username: String, token: String)
case class ChatConnection(
  ws: Option[WebSocket] = None,
  user: Dto.ChatUser = Dto.ChatUser(),
  users: Dto.ChatUsers = Dto.ChatUsers(),
  msgs: Vector[Dto.ChatMsg] = Vector()
)
case class RootModel(
  clicks: Clicks,
  randomNumber: Pot[Foo] = Empty,
  todos: Pot[Vector[TodoTask]] = Empty,
  scores: Pot[Vector[ScoreboardRecord]] = Empty,
  auth: Pot[Auth] = Empty,
  securedText: Pot[String] = Empty,
  chatConn: ChatConnection = ChatConnection(),
  globalName: String = "unknown"
)

case object IncreaseClicks extends Action
case class TryGetRandom(potResult: Pot[Foo] = Empty) extends PotAction[Foo, TryGetRandom] {
  def next(newResult: Pot[Foo]) = copy(potResult = newResult)
}

case class AddNewTodo(todoTask: TodoTask)                       extends Action
case class TodoAdded(todoTask: TodoTask)                        extends Action
case class SwitchTodoStatus(id: String)                         extends Action
case class TodoStatusSwitched(id: String, newState: TodoStatus) extends Action
case class DeleteTodo(id: String)                               extends Action
case class TodoDeleted(id: String)                              extends Action
case class TryGetTodos(potResult: Pot[Vector[TodoTask]] = Empty) extends PotAction[Vector[TodoTask], TryGetTodos] {
  def next(newResult: Pot[Vector[TodoTask]]) = copy(potResult = newResult)
}

case class AddNewScore(score: ScoreboardRecord) extends Action
case class ScoreAdded(score: ScoreboardRecord)  extends Action
case object ClearScoreboard                     extends Action
case object ScoreboardCleared                   extends Action
case class TryGetScoreboard(potResult: Pot[Vector[ScoreboardRecord]] = Empty)
    extends PotAction[Vector[ScoreboardRecord], TryGetScoreboard] {
  def next(newResult: Pot[Vector[ScoreboardRecord]]) = copy(potResult = newResult)
}

case object SignOut extends Action
case class TryAuth(username: String, passwd: String, potResult: Pot[Auth] = Empty) extends PotAction[Auth, TryAuth] {
  def next(newResult: Pot[Auth]) = copy(potResult = newResult)
}
case class TryRegister(username: String, passwd: String, potResult: Pot[Auth] = Empty)
    extends PotAction[Auth, TryRegister] {
  def next(newResult: Pot[Auth]) = copy(potResult = newResult)
}

case class TryGetSecuredText(token: String, potResult: Pot[String] = Empty)
    extends PotAction[String, TryGetSecuredText] {
  def next(newResult: Pot[String]) = copy(potResult = newResult)
}

case object Connect                      extends Action
case object ReConnect                    extends Action
case object Disconnect                   extends Action
case class Connected(user: Dto.ChatUser) extends Action
case object Disconnected                 extends Action

case class InitChatUsers(us: Dto.ChatUsers)  extends Action
case class AddNewMsg(msg: Dto.ChatMsg)       extends Action
case class AddUser(u: Dto.NewChatUser)       extends Action
case class RemoveUser(u: Dto.ChatUserLeft)   extends Action
case class ChangeMyChatName(newName: String) extends Action

case class SetGlobalName(newName: String) extends Action
case object RefreshGlobalName             extends Action

object AppCircuit extends Circuit[RootModel] {
  override protected def initialModel: RootModel = RootModel(Clicks(0))

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomTo(_.clicks)),
    new RandomNumberHandler(zoomTo(_.randomNumber)),
    new TodosHandler(zoomTo(_.todos)),
    new ScoreboardHandler(zoomTo(_.scores)),
    new AuthHandler(zoomTo(_.auth)),
    new SecuredTextHandler(zoomTo(_.securedText)),
    new WebsockLifecycleHandler(zoomTo(_.chatConn)),
    new ChatHandler(zoomTo(_.chatConn)),
    new GlobalNameHandler(zoomTo(_.globalName))
  )
}

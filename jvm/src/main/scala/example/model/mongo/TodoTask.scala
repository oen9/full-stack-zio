package example.model.mongo

object TodoTask {
  sealed trait TodoStatus
  case object Done extends TodoStatus
  case object Pending extends TodoStatus

  case class TodoTask(value: String, status: TodoStatus)

  import reactivemongo.api.bson.Macros
  import reactivemongo.api.bson.MacroOptions.{
    AutomaticMaterialization, UnionType, \/
  }
  object TodoStatus {
    type PredefinedTodoStatus = UnionType[Done.type \/ Pending.type] with AutomaticMaterialization
    implicit val predefinedTodoStatus = Macros.handlerOpts[TodoStatus, PredefinedTodoStatus]
  }
  implicit val todoTaskHandler = Macros.handler[TodoTask]
}

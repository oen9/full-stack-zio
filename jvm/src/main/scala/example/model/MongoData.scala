package example.model

import reactivemongo.api.bson.BSONObjectID
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.Macros.Annotations.Key

object MongoData {
  sealed trait TodoStatus
  case object Done    extends TodoStatus
  case object Pending extends TodoStatus

  case class TodoTask(@Key("_id") id: BSONObjectID, value: String, status: TodoStatus)

  object TodoStatus {
    import reactivemongo.api.bson.MacroOptions.{\/, AutomaticMaterialization, UnionType}
    type PredefinedTodoStatus = UnionType[Done.type \/ Pending.type] with AutomaticMaterialization
    implicit val predefinedTodoStatus = Macros.handlerOpts[TodoStatus, PredefinedTodoStatus]
  }

  implicit val todoTaskHandler = Macros.handler[TodoTask]

  def switchStatus(oldStatus: TodoStatus): TodoStatus = oldStatus match {
    case Pending => Done
    case Done    => Pending
  }
}

package example.modules.services

import cats.implicits._
import io.scalaland.chimney.dsl._
import zio._

import example.model.Errors.WrongMongoId
import example.model.MongoData
import example.modules.db.todoRepository.TodoRepository
import example.shared.Dto
import example.shared.Dto._
import reactivemongo.api.bson.BSONObjectID

object todoService {
  type TodoService = Has[TodoService.Service]

  object TodoService {
    trait Service {
      def getAll: Task[List[TodoTask]]
      def createNew(todoTask: TodoTask): Task[String]
      def switchStatus(id: String): Task[TodoStatus]
    }

    val live: ZLayer[TodoRepository, Nothing, Has[TodoService.Service]] = ZLayer.fromService { todoRepository =>
      new Service {
        def getAll: Task[List[Dto.TodoTask]] = for {
          allTodos <- todoRepository.getAll
          dtos = allTodos.map { _
            .into[TodoTask]
            .withFieldComputed(_.id, _.id.stringify.some)
            .transform
          }
        } yield dtos

        def createNew(toCreate: Dto.TodoTask): Task[String] = {
          val newId = BSONObjectID.generate()
          val toInsert = toCreate
            .into[MongoData.TodoTask]
            .withFieldComputed(_.id, _ => newId)
            .transform
          for {
            allTodos <- todoRepository.insert(toInsert)
          } yield newId.stringify
        }

        def switchStatus(id: String): Task[TodoStatus] = for {
          bsonId <- ZIO.fromTry(BSONObjectID.parse(id)).mapError(e => WrongMongoId(e.getMessage()))
          found <- todoRepository.findById(bsonId)
          newStatus = MongoData.switchStatus(found.status)
          _ <- todoRepository.updateStatus(bsonId, newStatus)
          dto = newStatus.into[TodoStatus].transform
        } yield dto
      }
    }
  }

  def getAll: ZIO[TodoService, Throwable, List[TodoTask]] =
    ZIO.accessM[TodoService](_.get.getAll)
  def createNew(toCreate: TodoTask): ZIO[TodoService, Throwable, String] =
    ZIO.accessM[TodoService](_.get.createNew(toCreate))
  def switchStatus(id: String): ZIO[TodoService, Throwable, Dto.TodoStatus] =
    ZIO.accessM[TodoService](_.get.switchStatus(id))
}

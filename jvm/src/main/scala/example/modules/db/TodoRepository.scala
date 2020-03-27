package example.modules.db

import example.model.Errors.TodoTaskNotFound
import example.model.MongoData._
import example.modules.db.MongoConn.MongoConn
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.BSONObjectID
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.Cursor
import zio._
import zio.logging.Logging
import zio.logging.LogLevel

object todoRepository {
  type TodoRepository = Has[TodoRepository.Service]

  object TodoRepository {
    trait Service {
      def getAll: Task[List[TodoTask]]
      def insert(todoTask: TodoTask): Task[WriteResult]
      def findById(id: BSONObjectID): Task[TodoTask]
      def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult]
    }

    val live = ZLayer.fromServices[MongoConn, Logging.Service, TodoRepository.Service]((mongoConn, logging) =>
      new Service {
        val collection: BSONCollection = mongoConn.defaultDb.collection("todos")
        val logger = logging.logger

        def getAll: Task[List[TodoTask]] = ZIO.fromFuture(implicit ec => {
          collection.find(BSONDocument(), Option.empty).cursor[TodoTask]().collect[List](-1, Cursor.FailOnError[List[TodoTask]]())
        })

        def insert(todoTask: TodoTask): Task[WriteResult] = ZIO.fromFuture(implicit ec => {
          collection.insert.one(todoTask)
        })

        def findById(id: BSONObjectID): Task[TodoTask] = {
          val query = BSONDocument("_id" -> id)
          for {
            maybeFound <- ZIO.fromFuture(implicit ec =>
              collection.find(query, Option.empty).one[TodoTask]
            )
            found <- ZIO.fromOption(maybeFound).flatMapError{ _ =>
              val msg = s"TodoTask '${id.stringify}' not found"
              logger.log(LogLevel.Trace)(s"findById $msg") as TodoTaskNotFound(msg)
            }
            _ <- logger.log(LogLevel.Trace)(s"findById '$id' found: $found")
          } yield found
        }

        def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult] = {
          val query = BSONDocument("_id" -> id)
          val update = BSONDocument("$set" -> BSONDocument("status" -> newStatus))
          for {
            updateResult <- ZIO.fromFuture(implicit ec =>
              collection.update.one(q = query, u = update)
            )
            _ <- logger.log(LogLevel.Trace)(s"updateStatus '$id' to $newStatus: $updateResult")
          } yield updateResult
        }
      }
    )
  }

  def getAll: ZIO[TodoRepository, Throwable, List[TodoTask]] =
    ZIO.accessM[TodoRepository](_.get.getAll)
  def insert(todoTask: TodoTask): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.insert(todoTask))
  def findById(id: BSONObjectID): ZIO[TodoRepository, Throwable, TodoTask] =
    ZIO.accessM[TodoRepository](_.get.findById(id))
  def updateStatus(id: BSONObjectID, newStatus: TodoStatus): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.updateStatus(id, newStatus))
}

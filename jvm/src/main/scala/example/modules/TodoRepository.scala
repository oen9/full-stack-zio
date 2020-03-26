package example.modules

import example.model.MongoData._
import example.modules.MongoConn.MongoConn
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
      def switchStatus(id: BSONObjectID): Task[TodoStatus]
      def runDebugTest(): Task[Unit]
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

        def switchStatus(id: BSONObjectID): Task[TodoStatus] = {
          def selectNewStatus(oldStatus: TodoStatus) = oldStatus match {
            case Pending => Done
            case Done => Pending
          }

          for {
            found <- findById(id)
            newStatus = selectNewStatus(found.status)
            _ <- updateStatus(id, newStatus)
          } yield newStatus
        }

        private def findById(id: BSONObjectID): Task[TodoTask] = {
          val query = BSONDocument("_id" -> id)
          for {
            maybeFound <- ZIO.fromFuture(implicit ec =>
              collection.find(query, Option.empty).one[TodoTask]
            )
            found <- ZIO.fromOption(maybeFound).flatMapError{ _ =>
              val msg = s"TodoTask '$id' not found"
              logger.log(LogLevel.Trace)(s"findById $msg") as new Exception(msg) // TODO create exception class
            }
            _ <- logger.log(LogLevel.Trace)(s"findById '$id' found: $found")
          } yield found
        }

        private def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult] = {
          val query = BSONDocument("_id" -> id)
          val update = BSONDocument("$set" -> BSONDocument("status" -> newStatus))
          for {
            updateResult <- ZIO.fromFuture(implicit ec =>
              collection.update.one(q = query, u = update)
            )
            _ <- logger.log(LogLevel.Trace)(s"updateStatus '$id' to $newStatus: $updateResult")
          } yield updateResult
        }

        def runDebugTest(): Task[Unit] = for {
          _ <- ZIO.unit
          someId = BSONObjectID.generate()
          todoTask = TodoTask(someId, "some: 42", Pending)
          writeResult <- insert(todoTask)
          _ <- logger.log(LogLevel.Debug)(s"writeResult: $writeResult")
          records <- getAll
          _ <- logger.log(LogLevel.Debug)(s"Mongo list: $records")
          newStatus <- switchStatus(someId)
          _ <- logger.log(LogLevel.Debug)(s"$someId status should be changed to $newStatus")
          updatedRecords <- getAll
          _ <- logger.log(LogLevel.Debug)(s"Mongo list: $updatedRecords")

        } yield ()
      }
    )
  }

  def getAll: ZIO[TodoRepository, Throwable, List[TodoTask]] =
    ZIO.accessM[TodoRepository](_.get.getAll)
  def insert(todoTask: TodoTask): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.insert(todoTask))
  def switchStatus(id: BSONObjectID): ZIO[TodoRepository, Throwable, TodoStatus] =
    ZIO.accessM[TodoRepository](_.get.switchStatus(id))
  def runDebugTest(): ZIO[TodoRepository, Throwable, Unit] =
    ZIO.accessM[TodoRepository](_.get.runDebugTest())
}

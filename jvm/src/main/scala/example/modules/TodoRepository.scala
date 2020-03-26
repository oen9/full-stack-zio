package example.modules

import example.model.mongo.TodoTask._
import example.modules.MongoConn.MongoConn
import reactivemongo.api.bson.BSONDocument
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
      def runDebugTest(): Task[Unit]
    }

    val live = ZLayer.fromServices[MongoConn, Logging.Service, TodoRepository.Service]((mongoConn, logging) =>
      new Service {
        val collection: BSONCollection = mongoConn.defaultDb.collection("todos")
        val logger = logging.logger

        def getAll: Task[List[TodoTask]] = ZIO.fromFuture(implicit ec => {
          collection.find(BSONDocument(), Option.empty).cursor[TodoTask]().collect[List](-1, Cursor.FailOnError[List[TodoTask]]())
        })

        def insert(todoTask: example.model.mongo.TodoTask.TodoTask): Task[WriteResult] = ZIO.fromFuture(implicit ec => {
          collection.insert.one(todoTask)
        })

        def runDebugTest(): Task[Unit] = for {
          _ <- ZIO.unit
          todoTask = TodoTask("some: 42", Pending)
          writeResult <- insert(todoTask)
          _ <- logger.log(LogLevel.Debug)(s"writeResult: $writeResult")
          records <- getAll
          _ <- logger.log(LogLevel.Debug)("Mongo list: " + records)
        } yield ()
      }
    )
  }

  def getAll: ZIO[TodoRepository, Throwable, List[TodoTask]] =
    ZIO.accessM[TodoRepository](_.get.getAll)
  def insert(todoTask: TodoTask): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.insert(todoTask))
  def runDebugTest(): ZIO[TodoRepository, Throwable, Unit] =
    ZIO.accessM[TodoRepository](_.get.runDebugTest())
}

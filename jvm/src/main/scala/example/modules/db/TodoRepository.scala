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
import zio.logging.Logger
import zio.logging.LogLevel

object todoRepository {
  type TodoRepository = Has[TodoRepository.Service]

  object TodoRepository {
    trait Service {
      def getAll: Task[List[TodoTask]]
      def insert(todoTask: TodoTask): Task[WriteResult]
      def findById(id: BSONObjectID): Task[TodoTask]
      def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult]
      def deleteById(id: BSONObjectID): Task[WriteResult]
    }

    def createNotFoundMsg(id: BSONObjectID) = s"TodoTask '${id.stringify}' not found"

    val live = ZLayer.fromServices[MongoConn, Logger[String], TodoRepository.Service]((mongoConn, logger) =>
      new Service {
        val collection: BSONCollection = mongoConn.defaultDb.collection("todos")

        def getAll: Task[List[TodoTask]] = ZIO.fromFuture { implicit ec =>
          collection
            .find(BSONDocument(), Option.empty)
            .cursor[TodoTask]()
            .collect[List](-1, Cursor.FailOnError[List[TodoTask]]())
        }

        def insert(todoTask: TodoTask): Task[WriteResult] = ZIO.fromFuture { implicit ec =>
          collection.insert.one(todoTask)
        }

        def findById(id: BSONObjectID): Task[TodoTask] = {
          val query = BSONDocument("_id" -> id)
          for {
            maybeFound <- ZIO.fromFuture(implicit ec => collection.find(query, Option.empty).one[TodoTask])
            found <- ZIO.fromOption(maybeFound).flatMapError { _ =>
              val msg = createNotFoundMsg(id)
              logger.log(LogLevel.Trace)(s"findById $msg").as(TodoTaskNotFound(msg))
            }
            _ <- logger.log(LogLevel.Trace)(s"findById '$id' found: $found")
          } yield found
        }

        def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult] = {
          val query  = BSONDocument("_id"  -> id)
          val update = BSONDocument("$set" -> BSONDocument("status" -> newStatus))
          for {
            updateResult <- ZIO.fromFuture(implicit ec => collection.update.one(q = query, u = update))
            _            <- logger.log(LogLevel.Trace)(s"updateStatus '$id' to $newStatus: $updateResult")
          } yield updateResult
        }

        def deleteById(id: BSONObjectID): Task[WriteResult] = {
          val query = BSONDocument("_id" -> id)
          for {
            removeResult <- ZIO.fromFuture(implicit ec => collection.delete.one(query))
            _            <- logger.log(LogLevel.Trace)(s"deleteById '$id' delete: $removeResult")
          } yield removeResult
        }
      }
    )

    def test(initData: Vector[TodoTask] = Vector()) = ZLayer.fromEffect {
      import com.softwaremill.quicklens._
      import reactivemongo.api.commands.DefaultWriteResult
      import reactivemongo.api.commands.UpdateWriteResult

      //If this gets removed we have to change results in Service.
      val defaultResult = ZIO.succeed(DefaultWriteResult(true, 1, Seq(), None, None, None))
      val updateResult  = ZIO.succeed(UpdateWriteResult(true, 1, 1, Seq(), Seq(), None, None, None))

      for {
        ref <- Ref.make(initData)
      } yield new Service {
        def getAll: Task[List[TodoTask]] =
          ref.get.map(_.toList)

        def insert(todoTask: TodoTask): Task[WriteResult] =
          ref.update(_ :+ todoTask) *> defaultResult

        def findById(id: BSONObjectID): Task[TodoTask] =
          for {
            maybeFound <- ref.get.map(_.find(_.id == id))
            found      <- ZIO.fromOption(maybeFound).mapError(_ => TodoTaskNotFound(createNotFoundMsg(id)))
          } yield found

        def updateStatus(id: BSONObjectID, newStatus: TodoStatus): Task[WriteResult] =
          ref.update(_.map { tt =>
            if (tt.id == id) tt.modify(_.status).setTo(newStatus)
            else tt
          }) *> updateResult

        def deleteById(id: BSONObjectID): Task[WriteResult] =
          ref.update(_.filter(_.id != id)) *> defaultResult
      }
    }
  }

  def getAll: ZIO[TodoRepository, Throwable, List[TodoTask]] =
    ZIO.accessM[TodoRepository](_.get.getAll)
  def insert(todoTask: TodoTask): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.insert(todoTask))
  def findById(id: BSONObjectID): ZIO[TodoRepository, Throwable, TodoTask] =
    ZIO.accessM[TodoRepository](_.get.findById(id))
  def updateStatus(id: BSONObjectID, newStatus: TodoStatus): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.updateStatus(id, newStatus))
  def deleteById(id: BSONObjectID): ZIO[TodoRepository, Throwable, WriteResult] =
    ZIO.accessM[TodoRepository](_.get.deleteById(id))
}

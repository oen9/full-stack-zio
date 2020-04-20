package example.modules.services

import cats.implicits._
import example.modules.db.scoreboardRepository.ScoreboardRepository
import example.shared.Dto.ScoreboardRecord
import zio._
import zio.logging.Logging
import zio.logging.LogLevel

object scoreboardService {
  type ScoreboardService = Has[ScoreboardService.Service]

  object ScoreboardService {
    trait Service {
      def addNew(newRecord: ScoreboardRecord): Task[ScoreboardRecord]
      def listScores(): Task[Vector[ScoreboardRecord]]
      def deleteAll(): Task[Unit]
    }

    val live: ZLayer[ScoreboardRepository with Logging, Nothing, ScoreboardService] =
      ZLayer.fromServices[ScoreboardRepository.Service, Logging.Service, ScoreboardService.Service] {
        (scoreboardRepository, logging) =>
          new Service {
            val logger = logging.logger

            def addNew(newRecord: ScoreboardRecord): Task[ScoreboardRecord] = for {
              inserted <- scoreboardRepository.insert(newRecord)
              _ <- logger.log(LogLevel.Trace)(s"new score record added: $inserted")
            } yield inserted

            def listScores(): Task[Vector[ScoreboardRecord]] = for {
              allScores <- scoreboardRepository.getAll()
              _ <- logger.log(LogLevel.Trace)(s"got: '${allScores.size}' scores")
            } yield allScores

            def deleteAll(): Task[Unit] = for {
              deleteResult <- scoreboardRepository.deleteAll()
              _ <- logger.log(LogLevel.Trace)(s"deleted: '$deleteResult' scores")
            } yield ()
          }
      }

    def test(data: Vector[ScoreboardRecord] = Vector()) = ZLayer.succeed(new Service {
      def addNew(newRecord: ScoreboardRecord): Task[ScoreboardRecord] = ZIO.succeed(newRecord.copy(id = 42L.some))
      def listScores(): Task[Vector[ScoreboardRecord]] = ZIO.succeed(data)
      def deleteAll(): Task[Unit] = ZIO.unit
    })
  }

  def addNew(newRecord: ScoreboardRecord): ZIO[ScoreboardService, Throwable, ScoreboardRecord] =
    ZIO.accessM[ScoreboardService](_.get.addNew(newRecord))
  def listScores(): ZIO[ScoreboardService, Throwable, Vector[ScoreboardRecord]] =
    ZIO.accessM[ScoreboardService](_.get.listScores())
  def deleteAll(): ZIO[ScoreboardService, Throwable, Unit] =
    ZIO.accessM[ScoreboardService](_.get.deleteAll())
}


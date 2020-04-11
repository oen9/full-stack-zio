package example.modules.services

import example.modules.db.scoreboardRepository.ScoreboardRepository
import example.modules.services.randomService.RandomService
import zio._
import zio.logging.Logging
import zio.logging.LogLevel

object scoreboardService {
  type ScoreboardService = Has[ScoreboardService.Service]

  object ScoreboardService {
    trait Service {
      def insertFoo: Task[Unit]
    }

    val live: ZLayer[ScoreboardRepository with RandomService with Logging, Nothing, ScoreboardService] =
      ZLayer.fromServices[ScoreboardRepository.Service, RandomService.Service, Logging.Service, ScoreboardService.Service] {
        (scoreboardRepository, randomService, logging) =>
          new Service {
            val logger = logging.logger
            def insertFoo: Task[Unit] = {
              val name = "Foo"
              for {
                randomScore <- randomService.getRandom
                id <- scoreboardRepository.insert(name, randomScore)
                _ <- logger.log(LogLevel.Info)(s"new Foo with id: $id")
              } yield ()
            }
          }
      }
  }

  def insertFoo: ZIO[ScoreboardService, Throwable, Unit] =
    ZIO.accessM[ScoreboardService](_.get.insertFoo)
}


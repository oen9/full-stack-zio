package example.modules.db

import example.modules.appConfig.AppConfig
import org.flywaydb.core.Flyway
import zio._

object flywayHandler {
  type FlywayHandler = Has[FlywayHandler.Service]

  object FlywayHandler {
    trait Service {
      def initDb: Task[Unit]
    }

    val live: ZLayer[AppConfig, Throwable, FlywayHandler] = ZLayer.fromFunction { appCfg =>
      new FlywayHandler.Service {
        def initDb: Task[Unit] = for {
          cfgData <- appCfg.get.load
          _ <- Task.effect {
            Flyway
              .configure()
              .dataSource(cfgData.postgres.url, "", "")
              .load()
              .migrate()
          }
        } yield ()
      }
    }

  }

  def initDb: ZIO[FlywayHandler, Throwable, Unit] =
    ZIO.accessM[FlywayHandler](_.get.initDb)
}

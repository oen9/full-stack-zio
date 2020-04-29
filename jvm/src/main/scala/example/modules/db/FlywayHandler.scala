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
        def initDb: Task[Unit] =
          for {
            cfgData <- appCfg.get.load
            _ <- Task.effect {
              Flyway
                .configure()
                .dataSource(
                  cfgData.sqldb.url,
                  cfgData.sqldb.username,
                  cfgData.sqldb.password
                )
                .load()
                .migrate()
            }
          } yield ()
      }
    }

    def test(locations: Seq[String] = Seq()): ZLayer[AppConfig, Throwable, FlywayHandler] = ZLayer.fromFunction {
      appCfg =>
        new FlywayHandler.Service {
          def initDb: Task[Unit] =
            for {
              cfgData <- appCfg.get.load
              _ <- Task.effect {
                val flyway = Flyway
                  .configure()
                  .locations(locations: _*)
                  .dataSource(
                    cfgData.sqldb.url,
                    cfgData.sqldb.username,
                    cfgData.sqldb.password
                  )
                  .load()
                flyway.clean()
                flyway.migrate()
              }
            } yield ()
        }
    }

  }

  def initDb: ZIO[FlywayHandler, Throwable, Unit] =
    ZIO.accessM[FlywayHandler](_.get.initDb)
}

package example.modules.db

import cats.effect.Blocker
import cats.implicits._
import doobie.hikari._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import example.modules.appConfig
import example.modules.appConfig.AppConfig
import example.modules.db.flywayHandler.FlywayHandler
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

object doobieTransactor {
  type DoobieTransactor = Has[Transactor[Task]]

  val live: ZLayer[Blocking with AppConfig with FlywayHandler, Throwable, DoobieTransactor] = ZLayer.fromManaged {
    ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
      for {
        _          <- flywayHandler.initDb.toManaged_
        cfg        <- appConfig.load.toManaged_
        blockingEC <- ZIO.accessM[Blocking](b => ZIO.succeed(b.get.blockingExecutor.asEC)).toManaged_
        connectEC = rt.platform.executor.asEC
        transactor <- HikariTransactor
          .newHikariTransactor[Task](
            cfg.sqldb.driver,
            cfg.sqldb.url,
            cfg.sqldb.username,
            cfg.sqldb.password,
            connectEC,
            Blocker.liftExecutionContext(blockingEC)
          )
          .toManaged
      } yield transactor
    }
  }
}

package example

import example.model.MongoData.TodoTask
import example.modules.appConfig.AppConfig
import example.modules.db.doobieTransactor
import example.modules.db.flywayHandler
import example.modules.db.scoreboardRepository
import example.modules.db.todoRepository
import example.modules.services.scoreboardService
import example.modules.services.todoService
import zio.blocking.Blocking
import zio.logging.Logging

object TestEnvs {
  sealed trait SqlInit
  case object SqlEmpty extends SqlInit
  case object SqlFull extends SqlInit

  val appConf = AppConfig.live
  val logging = Logging.ignore

  def testDoobieTransactor(initdb: SqlInit) = {
    val migrations = initdb match {
      case SqlEmpty => Seq("db/migration")
      case SqlFull => Seq("db/migration", "db/fulldb")
    }
    val flyway = appConf >>> flywayHandler.FlywayHandler.test(migrations)
    (Blocking.any ++ appConf ++ flyway) >>> doobieTransactor.live
  }

  def testScoreboardService(initdb: SqlInit) = {
    val scoreboardRepo = testDoobieTransactor(initdb) >>> scoreboardRepository.ScoreboardRepository.live
    (scoreboardRepo ++ logging) >>> scoreboardService.ScoreboardService.live
  }

  def todoRepo(initData: Vector[TodoTask] = Vector()) = todoRepository.TodoRepository.test(initData)
  def todoServ(initData: Vector[TodoTask] = Vector()) = todoRepo(initData) >>> todoService.TodoService.live
}

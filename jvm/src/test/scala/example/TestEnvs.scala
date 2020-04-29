package example

import example.model.MongoData.TodoTask
import example.modules.appConfig.AppConfig
import example.modules.db.doobieTransactor
import example.modules.db.flywayHandler
import example.modules.db.scoreboardRepository
import example.modules.db.todoRepository
import example.modules.db.userRepository
import example.modules.services.auth.authService
import example.modules.services.cryptoService.CryptoService
import example.modules.services.scoreboardService
import example.modules.services.todoService
import zio.blocking.Blocking
import zio.logging.Logging

object TestEnvs {
  sealed trait SqlInit
  case object SqlEmpty extends SqlInit
  case object SqlFull  extends SqlInit

  val appConf    = AppConfig.live
  val logging    = Logging.ignore
  val cryptoServ = CryptoService.test

  def appConf(h2DbName: String = "test") = AppConfig.test(h2DbName)

  def testDoobieTransactor(initdb: SqlInit, h2DbName: String) = {
    val migrations = initdb match {
      case SqlEmpty => Seq("db/migration")
      case SqlFull  => Seq("db/migration", "db/fulldb")
    }
    val testAppConf = appConf(h2DbName)
    val flyway      = testAppConf >>> flywayHandler.FlywayHandler.test(migrations)
    (Blocking.any ++ testAppConf ++ flyway) >>> doobieTransactor.live
  }

  def testScoreboardService(initdb: SqlInit, h2DbName: String = "scoreboardDB") = {
    val scoreboardRepo = testDoobieTransactor(initdb, h2DbName) >>> scoreboardRepository.ScoreboardRepository.live
    (scoreboardRepo ++ logging) >>> scoreboardService.ScoreboardService.live
  }

  def testAuthService(h2DbName: String = "authDB") = {
    val userRepo = testDoobieTransactor(SqlFull, h2DbName) >>> userRepository.UserRepository.live
    (cryptoServ ++ userRepo ++ logging) >>> authService.AuthService.live
  }

  def todoRepo(initData: Vector[TodoTask] = Vector()) = todoRepository.TodoRepository.test(initData)
  def todoServ(initData: Vector[TodoTask] = Vector()) = todoRepo(initData) >>> todoService.TodoService.live
}

package example.modules.db

import example.modules.appConfig.AppConfig
import example.modules.AppConfigData
import reactivemongo.api.AsyncDriver
import reactivemongo.api.DefaultDB
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.MongoConnection
import scala.concurrent.duration._
import zio._

object MongoConn {
  case class MongoConn(conn: MongoConnection, defaultDb: DefaultDB)

  def createDriver() = ZIO.effect(new AsyncDriver)
  def makeConnection(mongoUri: String) =
    for {
      driver          <- createDriver()
      mongoConnection <- ZIO.fromFuture(_ => driver.connect(mongoUri))
    } yield mongoConnection

  def loadDefaultDb(mongoConnection: MongoConnection, appConfigData: AppConfigData): ZIO[Any, Throwable, DefaultDB] =
    for {
      uri       <- ZIO.fromFuture(implicit ec => MongoConnection.fromString(appConfigData.mongo.uri))
      dbName    <- ZIO.fromOption(uri.db).mapError(_ => new Exception("Can't read default db name"))
      defaultDb <- ZIO.fromFuture(implicit ec => mongoConnection.database(dbName, FailoverStrategy(retries = 20))) // (retries = 20) == 32 seconds
    } yield defaultDb

  val live: ZLayer[AppConfig, Throwable, Has[MongoConn]] = ZLayer.fromFunctionManaged { appCfg =>
    Managed.make(
      for {
        cfg       <- appCfg.get.load
        conn      <- makeConnection(cfg.mongo.uri)
        defaultDb <- loadDefaultDb(conn, cfg)
      } yield MongoConn(conn, defaultDb)
    )(mConn => UIO(mConn.conn.close()(5.second)))
  }
}

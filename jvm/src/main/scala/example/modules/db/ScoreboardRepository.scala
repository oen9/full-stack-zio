package example.modules.db

import cats.implicits._
import doobie._
import doobie.implicits._
import example.modules.db.doobieTransactor.DoobieTransactor
import example.shared.Dto.ScoreboardRecord
import zio._
import zio.interop.catz._

object scoreboardRepository {
  type ScoreboardRepository = Has[ScoreboardRepository.Service]

  object ScoreboardRepository {
    trait Service {
      def insert(record: ScoreboardRecord): UIO[ScoreboardRecord]
      def getAll(): UIO[Vector[ScoreboardRecord]]
      def deleteAll(): UIO[Int]
    }

    val live: ZLayer[DoobieTransactor, Throwable, ScoreboardRepository] = ZLayer.fromService { xa =>
      new Service {
        def insert(record: ScoreboardRecord): zio.UIO[ScoreboardRecord] =
          SQL
            .insert(record)
            .withUniqueGeneratedKeys[ScoreboardRecord]("id", "name", "score")
            .transact(xa)
            .orDie

        def getAll(): UIO[Vector[ScoreboardRecord]] =
          SQL
            .selectAllSortedByScore
            .to[Vector]
            .transact(xa)
            .orDie

        def deleteAll(): UIO[Int] =
          SQL
            .deleteAll
            .run
            .transact(xa)
            .orDie
      }
    }
  }

  def insert(record: ScoreboardRecord): ZIO[ScoreboardRepository, Throwable, ScoreboardRecord] =
    ZIO.accessM[ScoreboardRepository](_.get.insert(record))
  def getAll(): ZIO[ScoreboardRepository, Throwable, Vector[ScoreboardRecord]] =
    ZIO.accessM[ScoreboardRepository](_.get.getAll())
  def deleteAll(): ZIO[ScoreboardRepository, Throwable, Int] =
    ZIO.accessM[ScoreboardRepository](_.get.deleteAll())

  object SQL {
    def insert(record: ScoreboardRecord): Update0 = sql"""
      INSERT INTO scoreboard (name, score)
      VALUES (${record.name}, ${record.score})
      """.update

    val selectAllSortedByScore: Query0[ScoreboardRecord] = sql"""
      SELECT *
      FROM scoreboard
      ORDER BY (score, name) DESC
    """.query[ScoreboardRecord]

    val deleteAll: Update0 = sql"""
    DELETE
    FROM scoreboard
    """.update
  }
}

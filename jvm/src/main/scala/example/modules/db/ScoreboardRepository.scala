package example.modules.db

import cats.implicits._
import cats.implicits._
import doobie._
import doobie.implicits._
import example.modules.db.doobieTransactor.DoobieTransactor
import zio._
import zio.interop.catz._

object scoreboardRepository {
  type ScoreboardRepository = Has[ScoreboardRepository.Service]

  object ScoreboardRepository {
    trait Service {
      def insert(name: String, score: Int): UIO[Long]
    }

    val live: ZLayer[DoobieTransactor, Throwable, ScoreboardRepository] = ZLayer.fromService { xa =>
      new Service {
        def insert(name: String, score: Int): UIO[Long] = {
          SQL
            .insert(name, score)
            .withUniqueGeneratedKeys[Long]("id")
            .transact(xa)
            .orDie
        }
      }
    }
  }

  def insert(name: String, score: Int): ZIO[ScoreboardRepository, Throwable, Long] =
    ZIO.accessM[ScoreboardRepository](_.get.insert(name, score))

  object SQL {
    def insert(name: String, score: Int): Update0 = sql"""
      INSERT INTO scoreboard (name, score)
      VALUES ($name, $score)
      """.update
  }
}

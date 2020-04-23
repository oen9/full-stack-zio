package example.modules.db

import doobie._
import doobie.implicits._
import example.model.SqlData
import example.modules.db.doobieTransactor.DoobieTransactor
import zio._
import zio.interop.catz._

object userRepository {
  type UserRepository = Has[UserRepository.Service]

  object UserRepository {
    trait Service {
      def getUserByToken(token: String): UIO[Option[SqlData.User]]
      def getUserByName(name: String): UIO[Option[SqlData.User]]
      def insert(record: SqlData.User): UIO[Option[SqlData.User]]
      def updateToken(id: Long, newToken: String): Task[SqlData.User]
    }

    val live: ZLayer[DoobieTransactor, Throwable, UserRepository] = ZLayer.fromService { xa =>
      new Service {
        def getUserByToken(token: String): UIO[Option[SqlData.User]] =
          SQL
            .selectUserByToken(token)
            .unique
            .transact(xa)
            .option

        def getUserByName(name: String): UIO[Option[SqlData.User]] =
          SQL
            .selectUserByName(name)
            .unique
            .transact(xa)
            .option

        def insert(record: SqlData.User): UIO[Option[SqlData.User]] =
          SQL
            .insert(record)
            .withUniqueGeneratedKeys[SqlData.User]("id", "name", "password", "token")
            .transact(xa)
            .option

        def updateToken(id: Long, newToken: String): Task[SqlData.User] =
          SQL
            .updateToken(id, newToken)
            .withUniqueGeneratedKeys[SqlData.User]("id", "name", "password", "token")
            .transact(xa)

      }
    }
  }

  object SQL {
    def selectUserByName(name: String): Query0[SqlData.User] = sql"""
      SELECT *
      FROM users
      WHERE name = $name
    """.query[SqlData.User]

    def selectUserByToken(token: String): Query0[SqlData.User] = sql"""
      SELECT *
      FROM users
      WHERE token = $token
    """.query[SqlData.User]

    def insert(record: SqlData.User): Update0 = sql"""
      INSERT INTO users (name, password, token)
      VALUES (${record.name}, ${record.password}, ${record.token})
      """.update

    def updateToken(id: Long, newToken: String): Update0 = sql"""
      UPDATE users
      SET token = $newToken
      WHERE id = $id
      """.update
  }

}

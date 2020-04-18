package example.modules.services

import cats.implicits._
import example.shared.Dto.ScoreboardRecord
import example.TestEnvs
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

object ScoreboardServiceTest extends DefaultRunnableSpec {
  def spec = suite("scoreboardService test")(
    testM("listScores() with full db") {
      val expected = Vector(
        ScoreboardRecord(2L.some, "unknown2", 20),
        ScoreboardRecord(3L.some, "unknown3", 15),
        ScoreboardRecord(1L.some, "unknown1", 10),
        ScoreboardRecord(5L.some, "bbb", 5),
        ScoreboardRecord(4L.some, "aaa", 5),
      )

      val program = for {
        result <- scoreboardService.listScores()
      } yield assert(result)(equalTo(expected))

      program.provideLayer {
        TestEnvs.testScoreboardService(TestEnvs.SqlFull)
      }
    },

    testM("listScores() with empty db") {
      val expected = Vector()

      val program = for {
        result <- scoreboardService.listScores()
      } yield assert(result)(equalTo(expected))

      program.provideLayer {
        TestEnvs.testScoreboardService(TestEnvs.SqlEmpty)
      }
    },

    testM("addNew()") {
      val toInsert = ScoreboardRecord(name = "foo", score = 42)

      val program = for {
        addNewResult <- scoreboardService.addNew(toInsert)
        expectedRecord = toInsert.copy(id = addNewResult.id)
        allRecords <- scoreboardService.listScores()
      } yield
        assert(addNewResult)(equalTo(expectedRecord)) &&
        assert(addNewResult.id)(isSome(anything)) &&
        assert(allRecords)(exists(equalTo(expectedRecord)))

      program.provideLayer {
        TestEnvs.testScoreboardService(TestEnvs.SqlEmpty)
      }
    },

    testM("deletaAll()") {
      val program = for {
        preDelete <- scoreboardService.listScores()
        _ <- scoreboardService.deleteAll()
        postDelete <- scoreboardService.listScores()
      } yield
        assert(preDelete)(Assertion.hasSize(equalTo(5))) &&
        assert(postDelete)(isEmpty)

      program.provideLayer {
        TestEnvs.testScoreboardService(TestEnvs.SqlFull)
      }
    },
  ) @@ sequential
}

package example.modules.services

import cats.implicits._
import com.softwaremill.quicklens._
import example.model.Errors.TodoTaskNotFound
import example.model.MongoData
import example.shared.Dto
import example.TestEnvs
import io.scalaland.chimney.dsl._
import reactivemongo.api.bson.BSONObjectID
import zio.test._
import zio.test.Assertion._

object TodoServiceTest extends DefaultRunnableSpec {
  val initData = Vector(
    MongoData.TodoTask(id = BSONObjectID.generate(), value = "foo", status = MongoData.Done),
    MongoData.TodoTask(id = BSONObjectID.generate(), value = "bar", status = MongoData.Pending),
  )

  val initDataAsDto = initData.map(_
    .into[Dto.TodoTask]
    .withFieldComputed(_.id, _.id.stringify.some)
    .transform
  ).toList

  def spec = suite("todoService test")(
    testM("getAll with full db") {
      val program = for {
        result <- todoService.getAll
      } yield assert(result)(equalTo(initDataAsDto))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("getAll with empty db") {
      val expected = List[Dto.TodoTask]()

      val program = for {
        result <- todoService.getAll
      } yield assert(result)(equalTo(expected))

      program.provideLayer(TestEnvs.todoServ())
    },

    testM("createNew") {
      val toCreate = Dto.TodoTask(value = "foo1", status = Dto.Pending)

      val program = for {
        createdId <- todoService.createNew(toCreate)
        allTodos <- todoService.getAll
        expected = toCreate.modify(_.id).setTo(createdId.some)
      } yield
        assert(createdId)(isNonEmptyString) &&
        assert(allTodos)(exists(equalTo(expected)))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("createNew with ignoring incoming id") {
      val toCreate = initDataAsDto.get(0).get

      val program = for {
        createdId <- todoService.createNew(toCreate)
        allTodos <- todoService.getAll
        expected = toCreate.modify(_.id).setTo(createdId.some)
      } yield
        assert(createdId)(not(equalTo(toCreate.id.get))) &&
        assert(allTodos)(exists(equalTo(expected)))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("switchStatus from Done") {
      val toSwitch = initDataAsDto.get(0).get

      val program = for {
        switched <- todoService.switchStatus(toSwitch.id.get)
        allTodos <- todoService.getAll
        expected = toSwitch.modify(_.status).setTo(Dto.Pending)
      } yield
        assert(switched)(equalTo(Dto.Pending)) &&
        assert(allTodos)(exists(equalTo(expected)))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("switchStatus from Pending") {
      val toSwitch = initDataAsDto.get(1).get

      val program = for {
        switched <- todoService.switchStatus(toSwitch.id.get)
        allTodos <- todoService.getAll
        expected = toSwitch.modify(_.status).setTo(Dto.Done)
      } yield
        assert(switched)(equalTo(Dto.Done)) &&
        assert(allTodos)(exists(equalTo(expected)))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("switchStatus NotFound") {
      val id = BSONObjectID.generate().stringify
      val program = todoService.switchStatus(id)

      val programWithLayers = program.provideLayer(TestEnvs.todoServ(initData))
      assertM(programWithLayers.run)(fails(isSubtype[TodoTaskNotFound](anything)))
    },

    testM("deleteTodo") {
      val expected = initDataAsDto.drop(1)

      val program = for {
        allTodos <- todoService.getAll
        _ <- todoService.deleteTodo(allTodos.get(0).get.id.get)
        actual <- todoService.getAll
      } yield assert(actual)(equalTo(expected))

      program.provideLayer(TestEnvs.todoServ(initData))
    },

    testM("deleteTodo NotFound") {
      val id = BSONObjectID.generate().stringify
      val program = todoService.deleteTodo(id)

      val programWithLayers = program.provideLayer(TestEnvs.todoServ(initData))
      assertM(programWithLayers.run)(fails(isSubtype[TodoTaskNotFound](anything)))
    },
  )
}

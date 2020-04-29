package example.endpoints

import cats.implicits._
import zio._
import zio.logging.Logging
import zio.test._
import zio.test.Assertion._

import io.circe.generic.extras.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

import example.Http4sTestHelper
import example.modules.services.todoService.TodoService
import example.shared.Dto._
import example.TestEnvs

object TodoEndpointsTest extends DefaultRunnableSpec {
  type TestEnv = TodoService with Logging

  val initData = List(
    TodoTask(id = "5e7ca3231200001200268a81".some, value = "foo", status = Pending),
    TodoTask(id = "5e7ca3231200001200268a82".some, value = "bar", status = Done),
    TodoTask(id = "5e7ca3231200001200268a83".some, value = "baz", status = Pending)
  )

  def spec = suite("TodoEndpoints")(
    testM("GET /todos") {
      val req = Request[RIO[TestEnv, *]](Method.GET, uri"/todos")

      val program = for {
        response   <- TodoEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, List[TodoTask]](response)
      } yield assert(parsedBody)(isSome(equalTo(initData)))

      program.provideLayer(
        TestEnvs.logging ++
          TodoService.test(initData)
      )
    },
    testM("POST /todos") {
      val postData = TodoTask(id = None, value = "foo", status = Pending)
      val req = Request[RIO[TestEnv, *]](Method.POST, uri"/todos")
        .withEntity(postData.asJson)

      val program = for {
        response   <- TodoEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, String](response)
      } yield assert(response.map(_.status))(isSome(equalTo(Status.Created))) &&
        assert(parsedBody)(isSome(isNonEmptyString))

      program.provideLayer(
        TestEnvs.logging ++
          TodoService.test()
      )
    },
    testM("POST /todos bad request") {
      val req = Request[RIO[TestEnv, *]](Method.POST, uri"/todos")
        .withEntity("some req")

      val program = for {
        response <- TodoEndpoints.routes[TestEnv].run(req).value
      } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

      program.provideLayer(
        TestEnvs.logging ++
          TodoService.test()
      )
    },
    testM("GET /todos/{id}/switch") {
      val id  = "5e7ca3231200001200268a81"
      val req = Request[RIO[TestEnv, *]](Method.GET, uri"/todos" / id / "switch")

      val program = for {
        response   <- TodoEndpoints.routes[TestEnv].run(req).value
        parsedBody <- Http4sTestHelper.parseBody[TestEnv, TodoStatus](response)
      } yield assert(response.map(_.status))(isSome(equalTo(Status.Ok))) &&
        assert(parsedBody)(isSome(isSubtype[TodoStatus](anything)))

      program.provideLayer(
        TestEnvs.logging ++
          TodoService.test()
      )
    },
    testM("DELETE /todos/{id}") {
      val id  = "5e7ca3231200001200268a81"
      val req = Request[RIO[TestEnv, *]](Method.DELETE, uri"/todos" / id)

      val program = for {
        response <- TodoEndpoints.routes[TestEnv].run(req).value
      } yield assert(response.map(_.status))(isSome(equalTo(Status.NoContent)))

      program.provideLayer(
        TestEnvs.logging ++
          TodoService.test()
      )
    }
  )
}

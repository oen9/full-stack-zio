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
import example.modules.services.auth.authService.AuthService
import example.shared.Dto._
import example.TestEnvs

object AuthEndpointsTest extends DefaultRunnableSpec {
  type TestEnv = AuthService with Logging

  val initData = Vector(
    User(id = 1L, name = "foo", token = "bar"),
    User(id = 2L, name = "test", token = "test")
  )

  val tokenHeader = "TOKEN"

  def spec = suite("AuthEndpoints")(
    suite("get user by token")(
      testM("GET /auth/user with token") {
        val expected = initData.get(0).get
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "user")
          .putHeaders(Header(tokenHeader, expected.token))

        val program = for {
          response   <- AuthEndpoints.routes[TestEnv].run(req).value
          parsedBody <- Http4sTestHelper.parseBody[TestEnv, User](response)
        } yield assert(parsedBody)(isSome(equalTo(expected)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(data = initData)
        )
      },
      testM("GET /auth/user with wrong token") {
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "user")
          .putHeaders(Header(tokenHeader, "wrong token"))

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.Unauthorized)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(data = initData)
        )
      },
      testM("GET /auth/user without token") {
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "user")

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      }
    ),
    suite("create new user")(
      testM("POST /auth/user with data") {
        val expected = initData.get(0).get
        val postData = AuthCredentials(name = expected.name, password = expected.token)
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth" / "user")
          .withEntity(postData.asJson)

        val program = for {
          response   <- AuthEndpoints.routes[TestEnv].run(req).value
          parsedBody <- Http4sTestHelper.parseBody[TestEnv, User](response)
        } yield assert(parsedBody)(isSome(equalTo(expected))) &&
          assert(response.map(_.status))(isSome(equalTo(Status.Created)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(newToken = expected.token)
        )
      },
      testM("POST /auth/user with corrupted data") {
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth" / "user")
          .withEntity("{corrupted = 'json")

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      },
      testM("POST /auth/user user exists") {
        val expected = initData.get(0).get
        val postData = AuthCredentials(name = expected.name, password = expected.token)
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth" / "user")
          .withEntity(postData.asJson)

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.Conflict)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(data = initData, newToken = expected.token)
        )
      }
    ),
    suite("generate new token")(
      testM("POST /auth with data") {
        val newToken = "new  token"
        val expected = initData.get(0).get.copy(token = newToken)
        val postData = AuthCredentials(name = expected.name, password = "not checked in tests")
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth")
          .withEntity(postData.asJson)

        val program = for {
          response   <- AuthEndpoints.routes[TestEnv].run(req).value
          parsedBody <- Http4sTestHelper.parseBody[TestEnv, User](response)
        } yield assert(parsedBody)(isSome(equalTo(expected)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(data = initData, newToken = expected.token)
        )
      },
      testM("POST /auth with corrupted data") {
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth")
          .withEntity("{corrupted = 'json")

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      },
      testM("POST /auth with wrong credentials") {
        val postData = AuthCredentials(name = "mistake", password = "not checked in tests")
        val req = Request[RIO[TestEnv, *]](Method.POST, uri"/auth")
          .withEntity(postData.asJson)

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.Unauthorized)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      }
    ),
    suite("get super secret text")(
      testM("GET /auth/secured with token") {
        val expected = "Super secure text"
        val user     = initData.get(0).get
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "secured")
          .putHeaders(Header(tokenHeader, user.token))

        val program = for {
          response   <- AuthEndpoints.routes[TestEnv].run(req).value
          parsedBody <- Http4sTestHelper.parseBody[TestEnv, String](response)
        } yield assert(parsedBody)(isSome(equalTo(expected))) &&
          assert(response.map(_.status))(isSome(equalTo(Status.Ok)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test(data = initData, secretReturn = expected)
        )
      },
      testM("GET /auth/secured without token") {
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "secured")

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.BadRequest)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      },
      testM("GET /auth/secured with wrong token") {
        val req = Request[RIO[TestEnv, *]](Method.GET, uri"/auth" / "secured")
          .putHeaders(Header(tokenHeader, "wrong token"))

        val program = for {
          response <- AuthEndpoints.routes[TestEnv].run(req).value
        } yield assert(response.map(_.status))(isSome(equalTo(Status.Unauthorized)))

        program.provideLayer(
          TestEnvs.logging ++ AuthService.test()
        )
      }
    )
  )
}

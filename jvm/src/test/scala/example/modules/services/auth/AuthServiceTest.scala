package example.modules.services.auth

import example.model.Errors.TokenNotFound
import example.modules.services.auth.authService
import example.shared.Dto.User
import example.TestEnvs
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import example.shared.Dto.AuthCredentials
import example.model.Errors.AuthenticationError
import example.model.Errors.UserExists

object AuthServiceTest extends DefaultRunnableSpec {
  val testUser = User(1L, "test", "test")
  val testUser2 = User(2L, "user2", "user2Token")
  val testUser2Cred = AuthCredentials("user2", "user2Password")

  def spec = suite("authService test")(
    suite("getUser")(
      testM("with correct token") {
        val program = for {
          result <- authService.getUser(testUser.token)
        } yield assert(result)(equalTo(testUser))

        program.provideLayer(TestEnvs.testAuthService())
      },

      testM("with wrong token") {
        val program = authService.getUser("wrong token")
        val programWithLayers = program.provideLayer(TestEnvs.testAuthService())
        assertM(programWithLayers.run)(fails(isSubtype[TokenNotFound](anything)))
      },
    ),

    suite("generateToken")(
      testM("correct credentials full flow") {
        val expected = testUser2.copy(token = "generatedToken")
        val program = for {
          oldUserData <- authService.getUser(testUser2.token)
          result <- authService.generateNewToken(testUser2Cred)
          newUserData <- authService.getUser(result.token)
        } yield
          assert(oldUserData)(equalTo(testUser2)) &&
          assert(result)(equalTo(expected)) &&
          assert(newUserData)(equalTo(expected))

        program.provideLayer(TestEnvs.testAuthService())
      },

      testM("deactivate old token") {
        val program = for {
          _ <- authService.getUser(testUser2.token)
          _ <- authService.generateNewToken(testUser2Cred)
          _ <- authService.getUser(testUser2.token)
        } yield ()

        val programWithLayers = program.provideLayer(TestEnvs.testAuthService())
        assertM(programWithLayers.run)(fails(isSubtype[TokenNotFound](anything)))
      },

      testM("with wrong password") {
        val wrongCredentials = testUser2Cred.copy(password = "wrong password")
        val program = authService.generateNewToken(wrongCredentials)

        val programWithLayers = program.provideLayer(TestEnvs.testAuthService())
        assertM(programWithLayers.run)(fails(isSubtype[AuthenticationError](anything)))
      },

      testM("with wrong name") {
        val wrongCredentials = testUser2Cred.copy(name = "wrong name")
        val program = authService.generateNewToken(wrongCredentials)

        val programWithLayers = program.provideLayer(TestEnvs.testAuthService())
        assertM(programWithLayers.run)(fails(isSubtype[AuthenticationError](anything)))
      },
    ),

    suite("createUser")(
      testM("create correct user full flow") {
        val newCred = AuthCredentials("newUserName", "somePassword")
        val program = for {
          created <- authService.createUser(newCred)
          userData <- authService.getUser(created.token)
        } yield
          assert(created.id)(not(isNull)) &&
          assert(created.name)(equalTo(newCred.name)) &&
          assert(created.token)(isNonEmptyString) &&
          assert(userData)(equalTo(created))

        program.provideLayer(TestEnvs.testAuthService())
      },

      testM("user exists") {
        val newCred = testUser2Cred.copy(password = "some passwd")
        val program = authService.createUser(newCred)

        val programWithLayers = program.provideLayer(TestEnvs.testAuthService())
        assertM(programWithLayers.run)(fails(isSubtype[UserExists](anything)))
      },
    ),

    testM("get secret text") {
      val program = for {
        result <- authService.secretText(testUser)
      } yield
        assert(result)(isNonEmptyString)

      program.provideLayer(TestEnvs.testAuthService())
    },

  ) @@ sequential
}

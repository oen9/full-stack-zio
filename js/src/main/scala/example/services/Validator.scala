package example.services

import cats.implicits._
import cats.kernel.Eq

object Validator {
  object Names {
    val username   = "username"
    val password   = "password"
    val rePassword = "repeat password"
  }

  def nonBlank(name: String, value: String): Either[Vector[String], String] =
    value.asRight.ensure(Vector(s"$name cannot be blank"))(_.trim.nonEmpty)

  def same[T: Eq](t1Name: String, t1: T, t2Name: String, t2: T): Either[Vector[String], T] =
    t1.asRight.ensure(Vector(s"$t1Name must be same as $t2Name"))(_ === t2)

  def validateTryAuth(username: String, password: String) =
    (
      nonBlank(Names.username, username).toValidated,
      nonBlank(Names.password, password).toValidated
    ).mapN(TryAuth(_, _))

  def validatePassword(password: String, rePassword: String): Either[Vector[String], String] = {
    val baseValidation = (
      nonBlank(Names.password, password).toValidated,
      nonBlank(Names.rePassword, rePassword).toValidated
    ).mapN((_, _)).toEither

    for {
      tup <- baseValidation
      (p, reP) = tup
      finalP <- same(
        Names.password,
        p,
        Names.rePassword,
        reP
      )
    } yield finalP
  }

  def validateTryRegister(username: String, password: String, rePassword: String) =
    (
      nonBlank(Names.username, username).toValidated,
      validatePassword(password, rePassword).toValidated
    ).mapN(TryRegister(_, _))
}

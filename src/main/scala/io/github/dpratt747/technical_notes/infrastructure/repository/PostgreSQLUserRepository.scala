package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.EitherT
import cats.effect.Bracket
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.User

final class PostgreSQLUserRepository[F[_] : Bracket[*[_], Throwable]](val connection: Transactor[F]) extends UserRepository[F] {

  def insertUser(user: User): EitherT[F, String, Int] =
    EitherT{
      sql"""INSERT INTO users (id, user_name, first_name, last_name, email, password)
           |VALUES (DEFAULT, ${user.userName.value},
           |${user.firstName.value}, ${user.lastName.value},
           |${user.email.value}, ${user.password.value})
           |RETURNING id""".stripMargin
        .query[Int]
        .unique
        .transact(connection)
        .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => "Username already exists please try a different one" }
    }

}

object PostgreSQLUserRepository {
  final def apply[F[_] : Bracket[*[_], Throwable]](connection: Transactor[F]): PostgreSQLUserRepository[F] =
    new PostgreSQLUserRepository[F](connection)
}

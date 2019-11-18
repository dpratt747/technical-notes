package repository

import cats.effect.IO
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.User
import io.github.dpratt747.technical_notes.domain.adt.values._
import io.github.dpratt747.technical_notes.infrastructure.repository.PostgreSQLUserRepository
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import test_utils.FunSpecWithFixtures

final class PostgreSQLUserRepositorySpec extends FunSpecWithFixtures with Matchers with ScalaFutures with OptionValues {

  describe("UserRepository") {

    val repo = new PostgreSQLUserRepository[IO](connection)
    val user = User(none, UserName("userName"), FirstName("firstName"), LastName("lastName"), Email("email"), Password("password"))

    it("should add a tag and return an int representing the generated primary key"){ flyway =>
      flyway.migrate
      repo.insertUser(user).value.unsafeRunSync shouldEqual Right(1)
    }

    it("should return an error when attempting to add a username that already exists") { flyway =>
      flyway.migrate()
      repo.insertUser(user).value.unsafeRunSync shouldEqual Right(1)
      repo.insertUser(user).value.unsafeRunSync shouldEqual Left("Username already exists please try a different one")
    }

  }

}
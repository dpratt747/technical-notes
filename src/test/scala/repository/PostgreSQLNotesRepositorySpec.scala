package repository

import cats.effect.IO
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.values.{Description, TagId, TagName, Term}
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.infrastructure.repository.PostgreSQLNotesRepository
import monocle.macros.syntax.lens._
import org.postgresql.util.PSQLException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, OptionValues}
import test_utils.FunSpecWithFixtures

import scala.language.postfixOps

final class PostgreSQLNotesRepositorySpec extends FunSpecWithFixtures with Matchers with ScalaFutures with OptionValues {

  describe("Notes Repository") {

    val row1 = Note(none, Term("docker ps"), Description("list docker process"), List(Tag(TagId(1).some, TagName("docker"))))
    val row2 = Note(none, Term("docker ps"), Description("list docker processesses"), List(Tag(TagId(1).some, TagName("docker"))))
    val repo = PostgreSQLNotesRepository[IO](connection)

    it("should add a note and return an int representing rows affected when non duplicate terms are passed"){ flyway =>
      flyway.migrate
      repo.insertNote(row1).unsafeRunSync shouldEqual 1
      repo.insertNote(row2.copy(term = Term("docker ps -a"))).unsafeRunSync shouldEqual 1
    }

    it("should complain when attempting to add a note with a term that already exists"){ flyway =>
      flyway.migrate
      repo.insertNote(row1).unsafeRunSync shouldEqual 1
      repo.insertNote(row1).unsafeRunSync shouldEqual 0
      repo.insertNote(row1).unsafeRunSync shouldEqual 0

//      val exception = the [PSQLException] thrownBy repo.insertNote(row2).unsafeRunSync
//      exception.getMessage should include("unique constraint")
    }

    it("should get a note by its term if it exists"){ flyway =>
      flyway.migrate
      repo.insertNote(row1).unsafeRunSync shouldEqual 1
      val query = repo.getNoteByTerm("docker ps").unsafeRunSync
      val expected = row1 lens(_.tags) set List.empty[Tag] lens(_.id) set 1.some
      query.value shouldEqual expected
    }

    it("should fail to get a note by its term if it does not exists"){ flyway =>
      flyway.migrate
      val query = repo.getNoteByTerm("docker ps").unsafeRunSync
      query.isDefined shouldBe false
    }

  }
}
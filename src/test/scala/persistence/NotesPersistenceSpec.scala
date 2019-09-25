package persistence

import adt.tables.NoteRow
import cats.implicits._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import test_utils.FunSpecWithFixtures

class NotesPersistenceSpec extends FunSpecWithFixtures with Matchers with ScalaFutures with OptionValues /*with DockerTestKit with DockerPostgresService*/ {

  describe("NotesPersistence") {

    it("should add one tag and return an int id"){ flyway =>
      flyway.migrate
      NotesPersistence.insertNote(NoteRow(none, "docker ps", "list docker process", List(1))).unsafeRunSync shouldEqual 1
      flyway.clean()
    }

    it("should complain when attempting to add a note with a term that already exists")(pending)

  }

}
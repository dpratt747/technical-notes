package service

import cats.effect.IO
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import repository.inMemory.{PostgreSQLInMemoryNotesRepository, PostgreSQLInMemoryTagsRepository}


final class NoteServiceSpec extends AnyFunSpec with Matchers {

  describe("Note Service") {

    val input = Note(none, "docker ps", "list docker container", List.empty[Tag])

    it("should take an input note and return a boolean representing its success or failure") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      service.addNote(input).unsafeRunSync shouldEqual true
    }

//    it("should get a list of stored tags") {
//      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
//      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
//      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
//      service.getTags.unsafeRunSync shouldBe an[Vector[Int]]
//    }

  }

}

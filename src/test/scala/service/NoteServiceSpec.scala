package service

import cats.data
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.values.{Description, Term}
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatest.funspec.AnyFunSpec
import repository.inMemory.{PostgreSQLInMemoryNotesRepository, PostgreSQLInMemoryTagsRepository}


final class NoteServiceSpec extends AnyFunSpec with Matchers {

  describe("Note Service") {

    it("should return 1 representing the number of rows inserted") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)

      val input = data.NonEmptyList.of(
        Note(none, Term("docker ps"), Description("list docker container"), List.empty[Tag])
      )
      service.addNote(input).unsafeRunSync shouldEqual NonEmptyList.of(1.asRight)

    }

    it("should take a List of notes and return a sequence of eithers representing success and failures") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)

      val input = NonEmptyList.of(
          Note(none, Term("docker ps"), Description("list docker container"), List.empty[Tag]),
          Note(none, Term("docker ps"), Description("list docker container"), List.empty[Tag]),
          Note(none, Term("docker ps"), Description("list docker container"), List.empty[Tag])
      )

      val action = service.addNote(input).unsafeRunSync

      action.collect{case Right(v) => v}.size shouldEqual 1
      action.collect{case Left(v) => v}.size shouldEqual 2
    }


  }

}

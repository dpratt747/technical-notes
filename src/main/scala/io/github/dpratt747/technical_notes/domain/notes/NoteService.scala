package io.github.dpratt747.technical_notes.domain.notes

import cats.{Id, Monad}
import cats.data.{NonEmptyList, Reader}
import cats.derived.auto.show._
import cats.implicits._
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.adt.values.TagId
import io.github.dpratt747.technical_notes.infrastructure.repository.{NotesRepository, TagsRepository}

final class NoteService[F[_]] {

  type AddNoteReader = Reader[(TagsRepository[F], NotesRepository[F], Transactor[F]), F[NonEmptyList[Either[String, Int]]]]

  def addNotes(input: NonEmptyList[Note])(implicit M: Monad[F]): AddNoteReader = Reader{ case (tagsRepo: TagsRepository[F], notesRepo: NotesRepository[F], connection: Transactor[F]) =>

    val action: F[NonEmptyList[(Int, Note)]] = input.map{ note =>
      val tagsF: F[List[Tag]] = note.tags.map{ tag =>
        tagsRepo.insertTagOrGetExisting(tag.tagName.value).run(connection).map(_.map(id => Tag(TagId(id).some, tag.tagName)))
      }.sequence

      for {
        tags <- tagsF
        n = Note(none, note.term, note.description, tags)
        insertNotCall <- notesRepo.insertNote(n).run(connection)
        rowsAffected <- insertNotCall
      } yield (rowsAffected, n)
    }.sequence

    action.map(_.map{ case (rowCount, note) =>
      rowCount.asRight[String].ensure(s"Note term already exists: ${note.show}")(_ > 0)
    })
  }

}

object NoteService {
  final def apply[F[_]]: NoteService[F] = new NoteService[F]
}

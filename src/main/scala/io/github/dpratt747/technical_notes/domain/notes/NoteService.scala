package io.github.dpratt747.technical_notes.domain.notes

import cats.Monad
import cats.data.NonEmptyList
import cats.derived.auto.show._
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.adt.values.TagId
import io.github.dpratt747.technical_notes.infrastructure.repository.{NotesRepository, PostgreSQLNotesRepository, PostgreSQLTagsRepository, TagsRepository}

class NoteService[F[_]](notesRepo: NotesRepository[F], tagsRepo: TagsRepository[F]) {

  final def addNote(input: NonEmptyList[Note])(implicit M: Monad[F]): F[NonEmptyList[Either[String, Int]]] = {

    val action: NonEmptyList[F[(Int, Note)]] = input map { note =>

      val tagsF: F[List[Tag]] = note.tags.map{ tag =>
        tagsRepo.insertTagOrGetExisting(tag.tagName.value).map(id => Tag(TagId(id).some, tag.tagName))
      }.traverse(identity)

      for {
        tags <- tagsF
        n = Note(none, note.term, note.description, tags)
        rowsAffected <- notesRepo.insertNote(n)
      } yield (rowsAffected, n)

    }

    action.sequence.map( _.map{ case (rowCount, note) =>
      rowCount.asRight[String].ensure(s"Note term already exists: ${note.show}")(_ > 0)
    })

  }

}

object NoteService {
  final def apply[F[_]](notesRepository: NotesRepository[F], tagsRepository: TagsRepository[F]): NoteService[F] =
    new NoteService[F](notesRepository, tagsRepository)
}

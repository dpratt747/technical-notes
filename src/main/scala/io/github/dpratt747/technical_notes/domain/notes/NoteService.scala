package io.github.dpratt747.technical_notes.domain.notes

import cats.{Functor, Monad}
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.adt.values.TagId
import io.github.dpratt747.technical_notes.infrastructure.repository.{NotesRepository, PostgreSQLNotesRepository, PostgreSQLTagsRepository, TagsRepository}

class NoteService[F[_]](notesRepo: NotesRepository[F], tagsRepo: TagsRepository[F]) {


  final def addNote(input: Note)(implicit M: Monad[F]): F[Boolean] = {

    val insertion: F[Int] = input.tags.map{ tag =>
      tagsRepo.insertTagOrGetExisting(tag.tagName.value).map(id => Tag(TagId(id).some, tag.tagName))
    }.traverse(identity).flatMap { tags =>
        notesRepo.insertNote(Note(none, input.term, input.description, tags))
    }

    // if the affected rows is greater than 0 the action succeeded else it failed
    insertion.map(_ > 0)
  }

//  final def getTags: F[Vector[Tags]]



}

object NoteService {
  final def apply[F[_]](notesRepository: NotesRepository[F], tagsRepository: TagsRepository[F]): NoteService[F] =
    new NoteService[F](notesRepository, tagsRepository)
}

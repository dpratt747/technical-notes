package io.github.dpratt747.technical_notes.infrastructure.repository

import doobie.util.Read
import io.github.dpratt747.technical_notes.domain.adt.Note

trait NotesRepository[F[_]] {
  def insertNote(note: Note): F[Int]
  def getNoteByTerm(term: String): F[Option[Note]]
}
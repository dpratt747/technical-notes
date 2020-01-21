package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.Reader
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Note

trait NotesRepository[F[_]] {
  def insertNote(note: Note): Reader[Transactor[F], F[Int]]
  def getNoteByTerm(term: String): Reader[Transactor[F], F[Option[Note]]]
}
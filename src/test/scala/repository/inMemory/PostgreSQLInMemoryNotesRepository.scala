package repository.inMemory
import cats._
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.infrastructure.repository.NotesRepository
import monocle.macros.GenLens

import scala.collection.concurrent.TrieMap
import scala.util.Random

class PostgreSQLInMemoryNotesRepository[F[_]: Applicative] extends NotesRepository[F] {

    private val cache = new TrieMap[Int, Note]

    def insertNote(note: Note): F[Int] = {
      val random = new Random(0)
      lazy val randomId: Option[Int] = random.nextInt.some
      val updatedNote: Note = GenLens[Note](_.id).set(randomId)(note)
      updatedNote.id.foreach {
        cache.put(_, updatedNote).pure[F]
      }
      1.pure[F]
    }

    def getNoteByTerm(term: String): F[Option[Note]] = {
      cache.find { case (_, note) => note.term == term }.map(_._2).pure[F]
    }

}

object PostgreSQLInMemoryNotesRepository {
  final def apply[F[_]: Applicative]() = new PostgreSQLInMemoryNotesRepository[F]()
}


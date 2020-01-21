package repository.inMemory
import cats._
import cats.data.Reader
import cats.implicits._
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.infrastructure.repository.NotesRepository
import monocle.macros.GenLens

import scala.collection.concurrent.TrieMap
import scala.util.Random

class PostgreSQLInMemoryNotesRepository[F[_]: Applicative] extends NotesRepository[F] {

    private val cache = new TrieMap[Int, Note]

    def insertNote(note: Note) = Reader{ _: Transactor[F] =>
      val random = new Random()
      lazy val randomId: Int = random.nextInt
      val updatedNote: Note = GenLens[Note](_.id).set(randomId.some)(note)
      val bool = cache.exists{ case (_, storedNote) => storedNote.term.value == note.term.value }
      if(bool) {
        0.pure[F]
      } else {
        cache.put(randomId, updatedNote).pure[F] *> 1.pure[F]
      }
    }

    def getNoteByTerm(term: String) = Reader{ _: Transactor[F] =>
      cache.find { case (_, note) => note.term.value == term }.map(_._2).pure[F]
    }

}

object PostgreSQLInMemoryNotesRepository {
  final def apply[F[_]: Applicative]() = new PostgreSQLInMemoryNotesRepository[F]
}


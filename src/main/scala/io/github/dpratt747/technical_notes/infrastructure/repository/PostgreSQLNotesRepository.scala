package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.Read
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.values.TagName
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}

class PostgreSQLNotesRepository[F[_]: Bracket[*[_], Throwable]](val connection: Transactor[F]) extends NotesRepository[F] {

  implicit val noteRead: Read[Note] = Read[(Int, String, String, List[Option[String]])].map {
    case (id, term, description, tagNames) =>
      val tags: List[Tag] = tagNames.sequence match {
        case Some(name) => name.map { name => Tag(none, TagName(name)) }
        case _ => List.empty[Tag]
      }
      Note(id.some, term, description, tags)
  }

  final def insertNote(note: Note): F[Int] = {
    val tagIds: List[Int] = note.tags.map(_.id.map(_.value)).sequence.getOrElse(List.empty[Int])
    sql"INSERT INTO notes (id, term, description, tags) VALUES (DEFAULT, ${note.term}, ${note.description}, ${tagIds})"
      .update
      .run
      .transact(connection)
  }

  final def getNoteByTerm(term: String): F[Option[Note]] =
    sql"""SELECT n.id, n.term, n.description, array_agg(t.tag) as tags FROM (SELECT id, term, description, UNNEST(tags) as tag FROM notes) n full outer join tags t ON n.tag = t.id WHERE n.term=$term group by (n.id, n.term, n.description)"""
      .query[Note]
      .option
      .transact(connection)

}

object PostgreSQLNotesRepository {
  final def apply[F[_] : Bracket[*[_], Throwable]](connection: Transactor[F]): PostgreSQLNotesRepository[F] =
    new PostgreSQLNotesRepository(connection)
}
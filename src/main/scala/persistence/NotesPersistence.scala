package persistence

import adt.tables.NoteRow
import cats.effect._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor.Aux

object NotesPersistence {

  final def insertNote(note: NoteRow)(implicit connection: Aux[IO, Unit]): IO[Int] =
    sql"INSERT INTO notes (id, term, description, tags) VALUES (DEFAULT, ${note.term}, ${note.description}, ${note.tags})"
      .update
      .run
      .transact(connection)


//  final def getTagById(id: Int)(implicit connection: Aux[IO, Unit]): IO[Option[TagRow]] =
//    sql"SELECT * FROM tags WHERE id = $id"
//      .query[TagRow]
//      .option
//      .transact(connection)
//
//  final def getTagByName(tagName: String)(implicit connection: Aux[IO, Unit]): IO[Option[TagRow]] =
//    sql"SELECT * FROM tags WHERE tag = ${tagName.toUpperCase}"
//      .query[TagRow]
//      .option
//      .transact(connection)


}

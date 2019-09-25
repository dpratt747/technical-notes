package persistence

import adt.tables.TagRow
import cats.effect._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

object TagsPersistence {

  final def insertTag(tagName: String)(implicit connection: Aux[IO, Unit]): IO[Int] =
    sql"INSERT INTO tags (id, tag) VALUES (DEFAULT, ${tagName.toUpperCase}) RETURNING id"
      .query[Int]
      .unique
      .transact(connection)

  final def getTagById(id: Int)(implicit connection: Aux[IO, Unit]): IO[Option[TagRow]] =
    sql"SELECT * FROM tags WHERE id = $id"
      .query[TagRow]
      .option
      .transact(connection)

  final def getTagByName(tagName: String)(implicit connection: Aux[IO, Unit]): IO[Option[TagRow]] =
    sql"SELECT * FROM tags WHERE tag = ${tagName.toUpperCase}"
      .query[TagRow]
      .option
      .transact(connection)


}

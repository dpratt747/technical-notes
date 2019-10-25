package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.effect.Bracket
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Tag

class PostgreSQLTagsRepository[F[_] : Bracket[*[_], Throwable]](val connection: Transactor[F]) extends TagsRepository[F] {

  final def insertTagOrGetExisting(tagName: String): F[Int] =
    sql"INSERT INTO tags (id, tag) VALUES (DEFAULT, ${tagName.toUpperCase}) RETURNING id"
      .query[Int]
      .unique
      .transact(connection)
      .exceptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => getExistingTagId(tagName) }

  final def getExistingTagId(tagName: String): F[Int] =
    sql"SELECT t.id FROM tags t WHERE tag = ${tagName.toUpperCase}"
      .query[Int]
      .unique
      .transact(connection)

  final def getTagById(id: Int): F[Option[Tag]] =
    sql"SELECT * FROM tags WHERE id = $id"
      .query[Tag]
      .option
      .transact(connection)

  final def getTagByName(tagName: String): F[Option[Tag]] =
    sql"SELECT * FROM tags WHERE tag = ${tagName.toUpperCase}"
      .query[Tag]
      .option
      .transact(connection)
}

object PostgreSQLTagsRepository {
  final def apply[F[_] : Bracket[*[_], Throwable]](connection: Transactor[F]): PostgreSQLTagsRepository[F] =
    new PostgreSQLTagsRepository(connection)
}

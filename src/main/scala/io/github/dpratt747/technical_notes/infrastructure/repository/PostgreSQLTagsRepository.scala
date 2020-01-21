package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.Reader
import cats.effect.Bracket
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Tag

class PostgreSQLTagsRepository[F[_] : Bracket[*[_], Throwable]] extends TagsRepository[F] {

  final def insertTagOrGetExisting(tagName: String): Reader[Transactor[F], F[Int]] = Reader { connection: Transactor[F] =>
    sql"INSERT INTO tags (id, tag) VALUES (DEFAULT, ${tagName.toUpperCase}) RETURNING id"
      .query[Int]
      .unique
      .transact(connection)
      .exceptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => getExistingTagId(tagName).run(connection) }
  }

  final def getExistingTagId(tagName: String): Reader[Transactor[F], F[Int]] = Reader { connection: Transactor[F] =>
    sql"SELECT t.id FROM tags t WHERE tag = ${tagName.toUpperCase}"
      .query[Int]
      .unique
      .transact(connection)
  }

  final def getTagById(id: Int): Reader[Transactor[F], F[Option[Tag]]] = Reader { connection: Transactor[F] =>
    sql"SELECT * FROM tags WHERE id = $id"
      .query[Tag]
      .option
      .transact(connection)
  }

  final def getTagByName(tagName: String): Reader[Transactor[F], F[Option[Tag]]] = Reader { connection: Transactor[F] =>
    sql"SELECT * FROM tags WHERE tag = ${tagName.toUpperCase}"
      .query[Tag]
      .option
      .transact(connection)
  }
}

object PostgreSQLTagsRepository {
  final def apply[F[_] : Bracket[*[_], Throwable]]: PostgreSQLTagsRepository[F] =
    new PostgreSQLTagsRepository
}

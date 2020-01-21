package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.Reader
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Tag

trait TagsRepository[F[_]] {
  def insertTagOrGetExisting(tagName: String): Reader[Transactor[F], F[Int]]
  def getTagById(id: Int): Reader[Transactor[F], F[Option[Tag]]]
  def getTagByName(tagName: String): Reader[Transactor[F], F[Option[Tag]]]
  def getExistingTagId(tagName: String): Reader[Transactor[F], F[Int]]
}

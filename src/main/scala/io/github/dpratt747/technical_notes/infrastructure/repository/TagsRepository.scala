package io.github.dpratt747.technical_notes.infrastructure.repository

import io.github.dpratt747.technical_notes.domain.adt.Tag

trait TagsRepository[F[_]] {
  def insertTagOrGetExisting(tagName: String): F[Int]
  def getTagById(id: Int): F[Option[Tag]]
  def getTagByName(tagName: String): F[Option[Tag]]
  def getExistingTagId(tagName: String): F[Int]
}

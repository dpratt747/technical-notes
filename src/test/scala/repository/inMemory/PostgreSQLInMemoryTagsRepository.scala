package repository.inMemory

import cats._
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.Tag
import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}
import io.github.dpratt747.technical_notes.infrastructure.repository.TagsRepository

import scala.collection.concurrent.TrieMap
import scala.util.Random

class PostgreSQLInMemoryTagsRepository[F[_]: Applicative] extends TagsRepository[F] {

  private val cache = new TrieMap[Int, String]

  final def insertTagOrGetExisting(tagName: String): F[Int] = {
    val random = new Random(0)
    lazy val randomId = random.nextInt
    getTagByName(tagName) map {
      case Some(value) => value.id.get.value
      case _ =>
        cache.put(randomId, tagName)
        randomId
    }
  }

  final def getTagById(id: Int): F[Option[Tag]] = {
    cache.get(id).map{ tag => Tag(TagId(id).some, TagName(tag)) }.pure[F]
  }

  final def getTagByName(tagName: String): F[Option[Tag]] = {
    cache.find { case (_, term) => term == tagName }
      .map{ case (id, tag) => Tag(TagId(id).some, TagName(tag)) }.pure[F]
  }

  final def getExistingTagId(tagName: String): F[Int] = {
    cache.find { case (_, term) => term == tagName }
      .map{ case (id, _) => id }.get.pure[F]
  }

}

object PostgreSQLInMemoryTagsRepository {
  def apply[F[_]: Applicative]() = new PostgreSQLInMemoryTagsRepository[F]()
}



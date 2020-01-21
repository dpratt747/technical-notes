package repository.inMemory

import cats._
import cats.data.Reader
import cats.implicits._
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.Tag
import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}
import io.github.dpratt747.technical_notes.infrastructure.repository.TagsRepository

import scala.collection.concurrent.TrieMap
import scala.util.Random

class PostgreSQLInMemoryTagsRepository[F[_]: Applicative] extends TagsRepository[F] {

  private val cache = new TrieMap[Int, String]

  final def insertTagOrGetExisting(tagName: String): Reader[Transactor[F], F[Int]] = Reader { c: Transactor[F] =>
    val random = new Random(0)
    lazy val randomId = random.nextInt
//    getTagByName(tagName)
//      .run(c)

    //    .map{
//      case Some(value: Tag) => value.id.get.value
//      case _ =>
//        cache.put(randomId, tagName)
//        randomId
//    }
    val x: Transactor[F] => Id[F[Option[Tag]]] = getTagByName(tagName).run(_)
    getTagByName(tagName).run(_).extract map { _.map {
            case Some(value) => value.id.get.value
            case _ =>
              cache.put(randomId, tagName)
              randomId
    }
    }
  }

  final def getTagById[F[_]: Applicative](id: Int): Reader[Transactor[F], F[Option[Tag]]] = Reader { _: Transactor[F] =>
    cache.get(id).map{ tag => Tag(TagId(id).some, TagName(tag)) }.pure[F]
  }

  final def getTagByName[F[_]: Applicative](tagName: String): Reader[Transactor[F], F[Option[Tag]]] = Reader { _: Transactor[F] =>
    cache.find { case (_, term) => term == tagName }
      .map{ case (id, tag) => Tag(TagId(id).some, TagName(tag)) }.pure[F]
  }

  final def getExistingTagId[F[_]: Applicative](tagName: String): Reader[Transactor[F], F[Int]] = Reader { _: Transactor[F] =>
    cache.find { case (_, term) => term == tagName }
      .map{ case (id, _) => id }.get.pure[F]
  }

}



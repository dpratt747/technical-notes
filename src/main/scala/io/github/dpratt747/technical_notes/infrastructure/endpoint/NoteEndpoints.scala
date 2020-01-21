package io.github.dpratt747.technical_notes.infrastructure.endpoint

import cats.Functor
import cats.data.{NonEmptyList, Reader}
import cats.effect.Sync
import cats.implicits._
import doobie.util.transactor.Transactor
import org.http4s.circe._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.Middleware
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.domain.adt.service.{HeaderType, UUID}
import io.github.dpratt747.technical_notes.domain.middleware.ValidateMandatoryHeaders
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import io.github.dpratt747.technical_notes.infrastructure.repository.{NotesRepository, TagsRepository}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

class NoteEndpoints[F[_] : Sync] extends Http4sDsl[F] with Middleware[F] with Codec {
  type NoteServiceReader = Reader[(NoteService[F], TagsRepository[F], NotesRepository[F], Transactor[F]), HttpRoutes[F]]

  private val mandatoryHeaders: Map[String, HeaderType] = Map("request-id" -> UUID)

  private final def handleInsertResult(input: NonEmptyList[Either[String, Int]]): F[Response[F]] = {
    val lefts = input.collect{ case Left(value) => value}
    if (lefts.isEmpty){
      Created()
    } else {
      BadRequest(lefts.asJson)
    }
  }

  /***
   * valid payload:
   * [
   *  {
   *    "id" : null,
   *    "term" : "docker ps",
   *    "description" : "list docker processes",
   *    "tags" : [
   *     {
   *       "id" : 1,
   *        "tagName" : "DOCKER"
   *      }
   *    ]
   *  }
   * ]
   */
  private final def postNote = Reader{  case (ns: NoteService[F], tr: TagsRepository[F], nr: NotesRepository[F], t: Transactor[F]) =>
    HttpRoutes.of[F] {
      case request@POST -> Root =>
        request.decode[NonEmptyList[Note]] {
          notes =>
            ns.addNotes(notes).run(tr, nr, t).map(_.flatMap(handleInsertResult))
        }
    }
  }

  private final def endpoints: NoteServiceReader = Reader{ case (ns: NoteService[F], tr: TagsRepository[F], nr: NotesRepository[F], t: Transactor[F]) =>
    ValidateMandatoryHeaders(postNote.run(ns, tr, nr, t), mandatoryHeaders)
  }
}

object NoteEndpoints {
  final def apply[F[_] : Sync]: NoteEndpoints[F]#NoteServiceReader = new NoteEndpoints[F].endpoints
}
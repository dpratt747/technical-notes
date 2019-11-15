package io.github.dpratt747.technical_notes.infrastructure.endpoint

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import org.http4s.circe._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.Middleware
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.domain.adt.service.{HeaderType, UUID}
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

class NoteEndpoints[F[_] : Sync] extends Http4sDsl[F] with Middleware[F] with Codec {

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
  private final def postNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root =>
      request.decode[NonEmptyList[Note]] {
        notes =>
          noteService.addNote(notes).flatMap(handleInsertResult)
      }
  }

  private final def endpoints(notesService: NoteService[F]): HttpRoutes[F] = {
    validateMandatoryHeaders(postNote(notesService), mandatoryHeaders)
  }
}

object NoteEndpoints {
  final def endpoints[F[_] : Sync](orderService: NoteService[F]): HttpRoutes[F] =
    new NoteEndpoints[F].endpoints(orderService)
}
package io.github.dpratt747.technical_notes.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.Middleware
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.domain.adt.service.{HeaderTypes, UUID}
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class NoteEndpoints[F[_] : Sync] extends Http4sDsl[F] with Middleware[F] with Codec {

  private val mandatoryHeaders: Map[String, HeaderTypes] = Map("request-id" -> UUID)

  /***
   * valid payload:
   * {
   *  "id" : null,
   *  "term" : "docker ps",
   *  "description" : "list docker processes",
   *  "tags" : [
   *    {
   *      "id" : 1,
   *      "tagName" : "DOCKER"
   *    }
   *  ]
   * }
   */
  private def postNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root =>
      for {
        note <- request.as[Note]
        bool <- noteService.addNote(note)
        res <- if (bool) Created() else InternalServerError()
      } yield res
  }

  final def endpoints(notesService: NoteService[F]): HttpRoutes[F] = {
    validateMandatoryHeaders(postNote(notesService), mandatoryHeaders)
  }
}

object NoteEndpoints {
  final def endpoints[F[_] : Sync](orderService: NoteService[F]): HttpRoutes[F] =
    new NoteEndpoints[F].endpoints(orderService)
}
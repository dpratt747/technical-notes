package io.github.dpratt747.technical_notes

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import io.github.dpratt747.technical_notes.infrastructure.endpoint.NoteEndpoints
import io.github.dpratt747.technical_notes.infrastructure.repository.{PostgreSQLNotesRepository, PostgreSQLTagsRepository, Transaction}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}
import org.slf4j.LoggerFactory

object WebServer extends IOApp with Transaction {

  final def startServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, H4Server[F]] = {
    for {
      _ <- Resource.liftF(LoggerFactory.getLogger(this.getClass).pure[F])
      tagsRepo = PostgreSQLTagsRepository[F](connection)
      notesRepo = PostgreSQLNotesRepository[F](connection)
      noteService = NoteService[F](notesRepo, tagsRepo)
      httpApp = Router(
      "/notes" -> NoteEndpoints.endpoints(noteService)
      ).orNotFound
      _ <- Resource.liftF(initDB)
      server <- BlazeServerBuilder[F]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .resource
    } yield server
  }

  final def run(args: List[String]): IO[ExitCode] = startServer.use(_ => IO.never).as(ExitCode.Success)

}

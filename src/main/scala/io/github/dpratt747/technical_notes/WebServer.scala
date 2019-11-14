package io.github.dpratt747.technical_notes

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits._
import io.github.dpratt747.technical_notes.domain.adt.configuration.Conf
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import io.github.dpratt747.technical_notes.infrastructure.endpoint.NoteEndpoints
import io.github.dpratt747.technical_notes.infrastructure.repository.{PostgreSQLNotesRepository, PostgreSQLTagsRepository, PostgresDetails}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object WebServer extends IOApp {

  final def startServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, H4Server[F]] = {
    val port: Int = scala.util.Properties.envOrElse("INTERNAL_APPLICATION_EXPOSED_PORT", "8080").toInt
    for {
      _ <- Resource.liftF(LoggerFactory.getLogger(this.getClass).pure[F])
      conf = ConfigSource.default.loadOrThrow[Conf]
      details = PostgresDetails(conf)
      tagsRepo = PostgreSQLTagsRepository[F](details.connection)
      notesRepo = PostgreSQLNotesRepository[F](details.connection)
      noteService = NoteService[F](notesRepo, tagsRepo)
      httpApp = Router(
      "/notes" -> NoteEndpoints.endpoints(noteService)
      ).orNotFound
      _ <- Resource.liftF(details.initDB)
      server <- BlazeServerBuilder[F]
        .bindHttp(port) //defaults to port 8080 and host IP
        .withHttpApp(httpApp)
        .resource
    } yield server
  }

  final def run(args: List[String]): IO[ExitCode] = startServer.use(_ => IO.never).as(ExitCode.Success)

}

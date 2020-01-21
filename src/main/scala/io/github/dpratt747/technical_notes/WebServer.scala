package io.github.dpratt747.technical_notes

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits._
import doobie.util.transactor.Transactor
import io.github.dpratt747.technical_notes.domain.adt.configuration.Conf
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import io.github.dpratt747.technical_notes.infrastructure.endpoint.{HealthEndpoints, NoteEndpoints}
import io.github.dpratt747.technical_notes.infrastructure.repository.{PostgreSQLNotesRepository, PostgreSQLTagsRepository, PostgresDetails}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.util.Properties

object WebServer extends IOApp {

  private final def startServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, H4Server[F]] = {
    val port: Int = Properties.envOrElse("INTERNAL_APPLICATION_PORT", "8080").toInt
    val host: String = Properties.envOrElse("APPLICATION_HOST_NAME", "localhost")

    for {
      logger <- Resource.liftF(LoggerFactory.getLogger(this.getClass).pure[F])
      conf = ConfigSource.default.loadOrThrow[Conf]
      details = PostgresDetails.apply
      transaction = details.connection.run(conf)
      httpApp = Router(
        "/notes" -> NoteEndpoints[F].run(NoteService[F], PostgreSQLTagsRepository[F], PostgreSQLNotesRepository[F], transaction),
        "/health" -> HealthEndpoints[F]
      ).orNotFound
      init <- details.initDB.run(conf)
      _ = Resource.liftF(init)
      server = BlazeServerBuilder[F]
        .bindHttp(port, host)
        .withHttpApp(httpApp)
        .resource
    } yield server
  }

  final def run(args: List[String]): IO[ExitCode] = startServer.use(_ => IO.never).as(ExitCode.Success)

}

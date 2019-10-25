package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.functor._
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import io.github.dpratt747.technical_notes.domain.adt.configuration.Conf
import org.flywaydb.core.Flyway
import pureconfig.ConfigSource
import pureconfig.generic.auto._

trait Transaction {

  val conf: Conf = ConfigSource.default.loadOrThrow[Conf]

  require(conf.postgres.port.value.isValidInt)
  require(conf.postgres.properties.databaseName.value.nonEmpty)
  require(conf.postgres.properties.user.value.nonEmpty)
  require(conf.postgres.properties.password.value.nonEmpty)

  final def connection[F[_]: Async: ContextShift]: Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    s"jdbc:postgresql://localhost:${conf.postgres.port.value}/${conf.postgres.properties.databaseName.value}",
    conf.postgres.properties.user.value,
    conf.postgres.properties.password.value
  )

  final def initDB[F[_]](implicit S: Sync[F]): F[Unit] =
      S.delay {
        val fw: Flyway =
          Flyway
            .configure()
            .dataSource(s"jdbc:postgresql://localhost:${conf.postgres.port.value}/${conf.postgres.properties.databaseName.value}", conf.postgres.properties.user.value,
              conf.postgres.properties.password.value)
            .load()

        fw.migrate()
      }.as(())

}

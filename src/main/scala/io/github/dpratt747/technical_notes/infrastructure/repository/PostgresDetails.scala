package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.Reader
import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.functor._
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import io.github.dpratt747.technical_notes.domain.adt.configuration.{Conf, DatabaseName, HostName, Password, Port, UserName}
import org.flywaydb.core.Flyway
import pureconfig.generic.auto._

final class PostgresDetails {

  private val url = Reader{conf: Conf =>
    s"jdbc:postgresql://${conf.postgres.hostName.value}:${conf.postgres.port.value}/${conf.postgres.properties.databaseName.value}"
  }

  def connection[F[_] : Async : ContextShift] = Reader { conf: Conf =>
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      url.run(conf), conf.postgres.properties.user.value, conf.postgres.properties.password.value
    )
  }

  def initDB[F[_]](implicit S: Sync[F]): Reader[Conf, F[Unit]] = Reader { conf: Conf =>
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(url.run(conf), conf.postgres.properties.user.value, conf.postgres.properties.password.value)
          .load()

      fw.migrate()
    }.as(())
  }

}

object PostgresDetails {
  def apply: PostgresDetails = new PostgresDetails
}
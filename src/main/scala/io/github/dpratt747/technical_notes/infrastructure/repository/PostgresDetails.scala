package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.functor._
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import io.github.dpratt747.technical_notes.domain.adt.configuration.Conf
import io.github.dpratt747.technical_notes.domain.adt.values._
import org.flywaydb.core.Flyway
import pureconfig.generic.auto._

final class PostgresDetails(conf: Conf){

  val postgresPort: Port = conf.postgres.port
  val databaseName: DatabaseName = conf.postgres.properties.databaseName
  val postgresUser: UserName = conf.postgres.properties.user
  val postgresPassword: Password = conf.postgres.properties.password
  val postgresHostName: HostName = conf.postgres.hostName

  require(postgresPort.value.isValidInt)
  require(databaseName.value.nonEmpty)
  require(postgresUser.value.nonEmpty)
  require(postgresPassword.value.nonEmpty)
  require(postgresHostName.value.nonEmpty)

  private val url = s"jdbc:postgresql://${postgresHostName.value}:${postgresPort.value}/${databaseName.value}"

  def connection[F[_] : Async : ContextShift]: Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    url, postgresUser.value, postgresPassword.value
  )

  def initDB[F[_]](implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(url, postgresUser.value, postgresPassword.value)
          .load()

      fw.migrate()
    }.as(())

}

object PostgresDetails {
  final def apply(conf: Conf): PostgresDetails = new PostgresDetails(conf)
}
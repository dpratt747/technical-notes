package persistence

import adt.configuration._
import cats.effect._
import doobie._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import pureconfig._
import pureconfig.generic.auto._

trait Transaction {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val conf: Conf = ConfigSource.default.loadOrThrow[Conf]

  require(conf.postgres.port.value.isValidInt)
  require(conf.postgres.properties.databaseName.value.nonEmpty)
  require(conf.postgres.properties.user.value.nonEmpty)
  require(conf.postgres.properties.password.value.nonEmpty)

  protected implicit val connection: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://localhost:${conf.postgres.port.value}/${conf.postgres.properties.databaseName.value}",
    conf.postgres.properties.user.value,
    conf.postgres.properties.password.value
  )

}

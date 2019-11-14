package test_utils

import java.sql.DriverManager

import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerKit, DockerReadyChecker}
import io.github.dpratt747.technical_notes.domain.adt.configuration.{Conf, DatabaseName, HostName, Password, Port, UserName}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

protected trait DockerPostgresService extends DockerKit {

  private val config = ConfigSource.default.loadOrThrow[Conf]
  val postgresPort: Port = config.postgres.port
  val databaseName: DatabaseName = config.postgres.properties.databaseName
  val postgresUser: UserName = config.postgres.properties.user
  val postgresPassword: Password = config.postgres.properties.password
  val postgresHostName: HostName = config.postgres.hostName

  private val random = scala.util.Random

  // max port number ranges between 49152 and 65535; opting to use lower bound
  private val upperBound = 49152 //todo: may not need to add or minus 80
  private lazy val randomPort = random.nextInt(upperBound)


  val postgresContainer: DockerContainer = {
    DockerContainer("postgres:latest")
      .withPorts((postgresAdvertisedPort, Some(postgresExposedPort)))
      .withEnv(s"POSTGRES_USER=${postgresUser.value}", s"POSTGRES_PASSWORD=${postgresPassword.value}", s"POSTGRES_DB=${databaseName.value}")
      .withReadyChecker(
    new PostgresReadyChecker(postgresUser.value, postgresPassword.value, Some(postgresExposedPort))
      .looped(15, 1.second)
    )
  }
  final def postgresAdvertisedPort: Int = 5432

  final def postgresExposedPort: Int = randomPort

  override def dockerContainers: List[DockerContainer] = {
    postgresContainer :: super.dockerContainers
  }
}

class PostgresReadyChecker(user: String, password: String, port: Option[Int] = None) extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(implicit docker: DockerCommandExecutor, ec: ExecutionContext): Future[Boolean] =
    container
      .getPorts()
      .map(ports =>
        Try {
          Class.forName("org.postgresql.Driver")
          val url = s"jdbc:postgresql://${docker.host}:${port.getOrElse(ports.values.head)}/"
          Option(DriverManager.getConnection(url, user, password)).map(_.close).isDefined
        }.getOrElse(false))

}
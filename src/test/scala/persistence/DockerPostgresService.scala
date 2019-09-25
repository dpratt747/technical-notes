package persistence

import java.sql.DriverManager

import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerKit, DockerReadyChecker}
import pureconfig.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DockerPostgresService extends DockerKit {

  private val r = scala.util.Random
  private val postgresUser = "postgres"
  private val postgresPassword = "docker"

  // max port number ranges between 49152 and 65535; opting to use lower bound
  private val upperBound = 49152 - 80
  private lazy val randomPort = 80 + r.nextInt(upperBound)

  val postgresContainer: DockerContainer = {
    DockerContainer("postgres:latest")
      .withPorts((postgresAdvertisedPort, Some(postgresExposedPort)))
      .withEnv(s"POSTGRES_USER=$postgresUser", s"POSTGRES_PASSWORD=$postgresPassword", "POSTGRES_DB=technical_notes_test")
      .withReadyChecker(
    new PostgresReadyChecker(postgresUser, postgresPassword, Some(postgresExposedPort))
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
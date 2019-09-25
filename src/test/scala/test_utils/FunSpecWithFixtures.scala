package test_utils

import cats.effect.{Blocker, ContextShift, IO}
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.flywaydb.core.Flyway
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, Outcome, fixture}
import persistence.DockerPostgresService
import cats.implicits._

trait FunSpecWithFixtures extends fixture.FunSpec with DockerTestKit with DockerPostgresService with Matchers {
  type FixtureParam = Flyway
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()
  private val url = s"jdbc:postgresql://localhost:$postgresExposedPort/technical_notes_test"
  private val user = "postgres"
  private val password = "docker"

  implicit val pc: PatienceConfig = PatienceConfig(Span(40, Seconds), Span(2, Seconds))
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  implicit val dockerFactory: DockerFactory = new SpotifyDockerFactory(client)
  implicit val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    url,
    user,
    password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // required for testing
  )

  final def withFixture(test: OneArgTest): Outcome = {
    val flyway: Flyway = Flyway.configure.dataSource(url, user, password).load
    dockerContainers.map(_.image.split(':').headOption) should contain("postgres".some)
    dockerContainers.forall(_.isReady().futureValue) shouldBe true
    test(flyway)
  }
}

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
import org.scalatest.{BeforeAndAfterEach, Matchers, Outcome}
import cats.implicits._
import org.scalatest.funspec.FixtureAnyFunSpec

trait FunSpecWithFixtures extends FixtureAnyFunSpec with BeforeAndAfterEach with DockerTestKit with DockerPostgresService with Matchers {
  type FixtureParam = Flyway
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()
  private val url = s"jdbc:postgresql://localhost:$postgresExposedPort/technical_notes_test"
  private val user = "postgres"
  private val password = "docker"
  private val flyway: Flyway = Flyway.configure.dataSource(url, user, password).load

  implicit val pc: PatienceConfig = PatienceConfig(Span(40, Seconds), Span(2, Seconds))
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  implicit val dockerFactory: DockerFactory = new SpotifyDockerFactory(client)
  val connection: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    url,
    user,
    password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // required for testing
  )

  override def afterEach() {
    flyway.clean()
  }

  final def withFixture(test: OneArgTest): Outcome = {
    dockerContainers.map(_.image.split(':').headOption) should contain("postgres".some)
    dockerContainers.forall(_.isReady().futureValue) shouldBe true
    test(flyway)
  }
}

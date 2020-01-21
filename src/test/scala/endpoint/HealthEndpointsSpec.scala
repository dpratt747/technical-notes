package endpoint

import cats.effect.IO
import io.github.dpratt747.technical_notes.infrastructure.endpoint.{Codec, HealthEndpoints}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{Request, Response, _}
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec

import scala.language.postfixOps

final class HealthEndpointsSpec extends AnyFunSpec with Matchers with Codec {

  describe("Health Endpoint") {

    it("should return a 200 when hit") {
      val endpoint: HttpRoutes[IO] = HealthEndpoints[IO]
      val router = Router(("/health", endpoint)).orNotFound

      val response: IO[Response[IO]] = router.run(
        Request(method = GET, uri = uri"/health")
      )

      Http4sServiceCheck(response, Ok)
    }
  }

}

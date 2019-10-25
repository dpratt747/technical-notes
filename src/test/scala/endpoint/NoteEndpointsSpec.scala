package endpoint

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}
import io.github.dpratt747.technical_notes.domain.adt.{Note, Tag}
import io.github.dpratt747.technical_notes.domain.notes.NoteService
import io.github.dpratt747.technical_notes.infrastructure.endpoint.{Codec, NoteEndpoints}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{Request, Response, _}
import org.scalatest.Matchers
import org.scalatest.funspec.AnyFunSpec
import repository.inMemory.{PostgreSQLInMemoryNotesRepository, PostgreSQLInMemoryTagsRepository}

import scala.language.postfixOps

final class NoteEndpointsSpec extends AnyFunSpec with Matchers with Codec {

  describe("Note Endpoints") {

    val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
    val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
    val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
    val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
    val router = Router(("/note", endpoint)).orNotFound

    it("should return a 400 if the mandatory headers are not sent with the request") {
      val body = Note(None, "docker ps", "list docker processes", List(Tag(TagId(1).some, TagName("DOCKER"))))
      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withEntity(body.asJson)
      )

      Http4sServiceCheck[Json](response, BadRequest, "Error with request: Missing mandatory headers".asJson some)
    }

    it("should accept a post request with a valid note format and return status OK(200)") {

      val body = Note(None, "docker ps", "list docker processes", List(Tag(TagId(1).some, TagName("DOCKER"))))
      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withHeaders(Header("request-id", java.util.UUID.randomUUID.toString))
          .withEntity(body.asJson)
      )

      Http4sServiceCheck(response, Created)
    }

  }

}

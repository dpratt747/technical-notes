package endpoint

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.adt.values.{Description, TagId, TagName, Term}
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

    it("should return a 400 if the mandatory headers are not sent with the request") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
      val router = Router(("/note", endpoint)).orNotFound

      val body = Note(None, Term("docker ps"), Description("list docker processes"), List(Tag(None, TagName("DOCKER"))))
      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withEntity(body.asJson)
      )

      Http4sServiceCheck[Json](response, BadRequest, "Errors with request: [ Missing mandatory header: request-id ]".asJson some)
    }

    it("should return a 400 if the mandatory headers are sent with an incorrect value type") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
      val router = Router(("/note", endpoint)).orNotFound

      val body = Note(None, Term("docker ps"), Description("list docker processes"), List(Tag(None, TagName("DOCKER"))))
      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withHeaders(Header("request-id", "this is definetly not a valid uuid"))
          .withEntity(body.asJson)

      )

      Http4sServiceCheck[Json](response, BadRequest, "Errors with request: [ The header was correctly provided but the header type does not match the expected type of: UUID ]".asJson some)
    }

    it("should accept a post request with a valid note format and return status 200") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
      val router = Router(("/note", endpoint)).orNotFound

      val note1 = Note(None, Term("docker ps"), Description("list docker processes"),
        List(
          Tag(None, TagName("DOCKER")),
          Tag(None, TagName("DOCKER22222"))
        )
      )
      val note2 = Note(None, Term("SELECT * FROM"), Description("some description1"), List(Tag(TagId(1).some, TagName("DOCKER"))))
      val note3 = Note(None, Term("some technical term"), Description("some description2"), List(Tag(TagId(1).some, TagName("DOCKER"))))
      val note4 = Note(None, Term("kubectl"), Description("some description3"), List(Tag(TagId(1).some, TagName("DOCKER"))))

      val payload  = NonEmptyList.of(note1, note2, note3, note4).asJson

      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withHeaders(Header("request-id", java.util.UUID.randomUUID.toString))
          .withEntity(payload)
      )

      Http4sServiceCheck(response, Created)
    }

    it("should return bad request when duplicate notes (repeated terms) are sent in payload") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
      val router = Router(("/note", endpoint)).orNotFound

      val note1 = Note(None, Term("docker ps"), Description("list docker processes"),
        List(
          Tag(None, TagName("DOCKER")),
          Tag(None, TagName("DOCKER22222"))
        )
      )
      val note2 = Note(None, Term("SELECT * FROM"), Description("some description1"), List(Tag(TagId(1).some, TagName("DOCKER"))))
      val note3 = Note(None, Term("some technical term"), Description("some description2"), List(Tag(TagId(1).some, TagName("DOCKER"))))
      val note4 = Note(None, Term("kubectl"), Description("some description3"), List(Tag(TagId(1).some, TagName("DOCKER"))))
      val note5 = Note(None, Term("SELECT * FROM"), Description("some description1"), List(Tag(TagId(1).some, TagName("DOCKER"))))

      val payload  = NonEmptyList.of(note1, note2, note3, note4, note5).asJson

      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withHeaders(Header("request-id", java.util.UUID.randomUUID.toString))
          .withEntity(payload)
      )


      Http4sServiceCheck(response, BadRequest)
    }

    it("should fail when sent an empty list as a payload") {
      val noteRepo = PostgreSQLInMemoryNotesRepository[IO]()
      val tagsRepo = PostgreSQLInMemoryTagsRepository[IO]()
      val service: NoteService[IO] = NoteService[IO](noteRepo, tagsRepo)
      val endpoint: HttpRoutes[IO] = NoteEndpoints.endpoints[IO](service)
      val router = Router(("/note", endpoint)).orNotFound

      val payload  = "[]".asJson

      val response: IO[Response[IO]] = router.run(
        Request(method = POST, uri = uri"/note")
          .withHeaders(Header("request-id", java.util.UUID.randomUUID.toString))
          .withEntity(payload)
      )

      Http4sServiceCheck(response, UnprocessableEntity)
    }

  }

}

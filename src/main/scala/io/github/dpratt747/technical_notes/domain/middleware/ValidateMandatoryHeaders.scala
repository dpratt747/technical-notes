package io.github.dpratt747.technical_notes.domain.middleware

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.adt.service.HeaderType
import org.http4s.circe._
import org.http4s.dsl.io.BadRequest
import org.http4s.{Header, HttpRoutes, Request, Response, Status}

final class ValidateMandatoryHeaders[F[_] : Monad] {

  type Message = String

  private def badRequestService(a: OptionT[F, Response[F]], b: Option[Message]): OptionT[F, Response[F]] =
    b match {
      case Some(message) => a.map(_.withStatus(BadRequest).withEntity(s"Errors with request: $message".asJson))
      case _ => a.map(_.withStatus(BadRequest))
    }

  private val convertReqHeadersToMap: Request[F] => Map[String, String] =
    _.headers.toList.map(header => header.name.toString -> header.value).toMap

  def validateMandatoryHeaders(service: HttpRoutes[F], mandatoryHeaders: Map[String, HeaderType]): HttpRoutes[F] =
    Kleisli { req: Request[F] =>
      val requestHeaders = convertReqHeadersToMap(req)

      val validate: List[Either[String, String]] = mandatoryHeaders.map { case (header, headerType) =>
        requestHeaders.get(header) match {
          case Some(value) =>
            if (headerType.validate(value).isDefined) {
              Right(value)
            } else {
              Left(s"The header was correctly provided but the header type does not match the expected type of: ${headerType.getClass.getSimpleName.filterNot(_ == '$')}")
            }
          case None =>
            Left(s"Missing mandatory header: $header")
        }
      }.toList

      validate collect { case Left(value) => value } match {
        case Nil => service(req)
        case list @ _::_ => badRequestService(service(req), s"[ ${list.mkString(", ")} ]" some)
      }

    }


}

object ValidateMandatoryHeaders {
  final def apply[F[_]: Monad](service: HttpRoutes[F], mandatoryHeaders: Map[String, HeaderType]): HttpRoutes[F] = new ValidateMandatoryHeaders[F].validateMandatoryHeaders(service, mandatoryHeaders)
}

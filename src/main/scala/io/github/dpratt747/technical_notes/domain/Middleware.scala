package io.github.dpratt747.technical_notes.domain

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import io.circe.syntax._
import io.github.dpratt747.technical_notes.domain.adt.service.HeaderTypes
import org.http4s.circe._
import org.http4s.dsl.io.BadRequest
import org.http4s.{Header, HttpRoutes, Request, Response, Status}

trait Middleware[F[_]] {

  type Message = String

  private final def badRequestService(a: OptionT[F, Response[F]], b: Option[Message])(implicit F: Monad[F]): OptionT[F, Response[F]] =
  b match {
      case Some(message) => a.map(_.withStatus(BadRequest).withEntity(s"Error with request: $message".asJson))
      case _ => a.map(_.withStatus(BadRequest))
    }

  private val convertReqHeadersToMap: Request[F] => Map[String, String] =
    _.headers.toList.map(header => header.name.toString -> header.value).toMap

  final def addHeadersToSuccess(service: HttpRoutes[F], header: Header)(implicit F: Monad[F]): HttpRoutes[F] = Kleisli { req: Request[F] =>
    service(req).map {
      case Status.Successful(resp) =>
        resp.putHeaders(header)
      case resp =>
        resp
    }
  }

  final def validateMandatoryHeaders(service: HttpRoutes[F], mandatoryHeaders: Map[String, HeaderTypes])(implicit F: Monad[F]): HttpRoutes[F] =
    Kleisli { req: Request[F] =>

      val containsRequired = mandatoryHeaders.keys.forall { header => convertReqHeadersToMap(req).contains(header) }

      if (containsRequired) {
        service(req)
      } else {
        badRequestService(service(req), "Missing mandatory headers".some)
      }
    }


}

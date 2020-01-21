package io.github.dpratt747.technical_notes.domain.middleware

import cats.Monad
import cats.data.Kleisli
import org.http4s.{Header, HttpRoutes, Request, Status}

final class AddHeadersToSuccess[F[_]: Monad](service: HttpRoutes[F], header: Header) {
  Kleisli { req: Request[F] =>
    service(req).map {
      case Status.Successful(resp) =>
        resp.putHeaders(header)
      case resp =>
        resp
    }
  }
}

object AddHeadersToSuccess {
  final def apply[F[_]: Monad](service: HttpRoutes[F], header: Header) = new AddHeadersToSuccess[F](service, header)
}

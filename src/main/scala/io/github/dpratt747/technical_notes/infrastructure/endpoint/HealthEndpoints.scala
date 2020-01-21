package io.github.dpratt747.technical_notes.infrastructure.endpoint

import cats.effect.Sync
import io.github.dpratt747.technical_notes.domain.Middleware
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class HealthEndpoints[F[_] : Sync] extends Http4sDsl[F] with Middleware[F] with Codec {

  private final def getHealth: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok()
  }

  private val endpoints: HttpRoutes[F] = getHealth
}

object HealthEndpoints {
  final def apply[F[_] : Sync]: HttpRoutes[F] = new HealthEndpoints[F].endpoints
}


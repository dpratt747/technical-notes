package io.github.dpratt747.technical_notes.infrastructure.repository

import cats.data.EitherT
import io.github.dpratt747.technical_notes.domain.adt.User

trait UserRepository[F[_]] {
  def insertUser(user: User): EitherT[F, String, Int]
}
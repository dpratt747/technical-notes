package io.github.dpratt747.technical_notes.domain

import cats.data.Kleisli
import shapeless.ops.tuple.Selector
import shapeless.{Generic, HList}

object KleisliSyntax {
  implicit class KleisliOps[F[_], A, B, L <: HList](f: Kleisli[F, A, B]){
    final def liftDep[AA](implicit ga: Generic.Aux[AA, L], sel: Selector[L, A]): Kleisli[F, AA, B] =
      f.local[AA](aa => sel.apply(ga.to(aa)))
  }
}

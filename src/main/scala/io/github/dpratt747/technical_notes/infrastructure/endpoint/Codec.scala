package io.github.dpratt747.technical_notes.infrastructure.endpoint
import cats.data.NonEmptyList
import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import io.github.dpratt747.technical_notes.domain.adt.Note
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}
import shapeless.Unwrapped

trait Codec {

  implicit def notesDecoder[F[_]: Sync]: EntityDecoder[F, NonEmptyList[Note]] = jsonOf[F, NonEmptyList[Note]]
  implicit def notesEncoder[F[_]: Sync]: EntityEncoder[F, NonEmptyList[Note]] = jsonEncoderOf[F, NonEmptyList[Note]]

  implicit def decodeAnyVal[T, U](
                                   unwrapped: Unwrapped.Aux[T, U],
                                   decoder: Decoder[U]): Decoder[T] = Decoder.instance[T] { cursor =>

    decoder(cursor).map(value => unwrapped.wrap(value))
  }

  implicit def encodeAnyVal[T, U](
                                   unwrapped: Unwrapped.Aux[T, U],
                                   encoder: Encoder[U]): Encoder[T] = Encoder.instance[T] { value =>

    encoder(unwrapped.unwrap(value))
  }

}


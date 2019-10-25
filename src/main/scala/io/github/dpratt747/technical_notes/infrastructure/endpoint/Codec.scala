package io.github.dpratt747.technical_notes.infrastructure.endpoint
import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import io.github.dpratt747.technical_notes.domain.adt.Note
import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

trait Codec {

  implicit def noteDecoder[F[_]: Sync]: EntityDecoder[F, Note] = jsonOf[F, Note]
  implicit def noteEncoder[F[_]: Sync]: EntityEncoder[F, Note] = jsonEncoderOf[F, Note]

  implicit val tagIdValueClassEncoder: Encoder[TagId] = deriveUnwrappedEncoder
  implicit val tagIdValueDecoder: Decoder[TagId] = deriveUnwrappedDecoder

  implicit val tagNameValueClassEncoder: Encoder[TagName] = deriveUnwrappedEncoder
  implicit val tagNameValueDecoder: Decoder[TagName] = deriveUnwrappedDecoder

}


package io.github.dpratt747.technical_notes.domain.adt.service

import scala.util.{Failure, Success, Try}

sealed trait HeaderType extends Product with Serializable {
  def validate[A <: Any]: A => Option[HeaderType]
}

case object UUID extends HeaderType {

  final def validate[A <: Any]: A => Option[HeaderType] = (id: A) => Try(java.util.UUID.fromString(id.toString)) match {
    case Success(_) => Some(this)
    case Failure(_) => None
  };

}
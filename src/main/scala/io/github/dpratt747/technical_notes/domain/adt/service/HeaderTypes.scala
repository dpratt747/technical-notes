package io.github.dpratt747.technical_notes.domain.adt.service

sealed trait HeaderTypes extends Product with Serializable
case object UUID extends HeaderTypes
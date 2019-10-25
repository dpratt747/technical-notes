package io.github.dpratt747.technical_notes.domain.adt

import io.github.dpratt747.technical_notes.domain.adt.values.{TagId, TagName}

final case class Tag(id: Option[TagId], tagName: TagName)

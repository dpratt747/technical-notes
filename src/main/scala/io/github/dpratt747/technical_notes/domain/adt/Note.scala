package io.github.dpratt747.technical_notes.domain.adt

import io.github.dpratt747.technical_notes.domain.adt.values.TagId

final case class Note(id: Option[Int], term: String, description: String, tags: List[Tag])

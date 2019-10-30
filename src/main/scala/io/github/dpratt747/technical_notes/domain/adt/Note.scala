package io.github.dpratt747.technical_notes.domain.adt

import io.github.dpratt747.technical_notes.domain.adt.values.{Description, TagId, Term}

final case class Note(id: Option[Int], term: Term, description: Description, tags: List[Tag])

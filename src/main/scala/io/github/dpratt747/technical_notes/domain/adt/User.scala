package io.github.dpratt747.technical_notes.domain.adt

import io.github.dpratt747.technical_notes.domain.adt.values._

final case class User(id: Option[Long], userName: UserName, firstName: FirstName, lastName: LastName, email: Email, password: Password)

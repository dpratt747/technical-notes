package adt.tables

sealed trait NoteTable extends Object with Serializable

final case class NoteRow(id: Option[Int], term: String, description: String, tags: List[Int])

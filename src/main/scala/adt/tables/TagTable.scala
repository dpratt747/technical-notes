package adt.tables

sealed trait TagTable extends Object with Serializable

final case class TagRow(id: Option[Int], tag: String) extends TagTable
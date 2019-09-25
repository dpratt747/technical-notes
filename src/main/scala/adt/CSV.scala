package adt

sealed trait CSV extends Product with Serializable

final case class Row(name: String, category: String) extends CSV
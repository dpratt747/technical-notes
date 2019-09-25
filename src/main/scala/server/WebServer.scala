package server

import adt.tables.NoteRow
import cats.implicits.none
import persistence.{NotesPersistence, TagsPersistence, Transaction}


object WebServer extends App with Transaction {

//  val x = TagsPersistence.insertTag("meme").unsafeRunSync
//  val Some(x) = TagsPersistence.getTagByName("meme").unsafeRunSync
//  println(NotesPersistence.insertNote(NoteRow(none, "docker ps", "list docker process", List(1))).unsafeRunSync)
}
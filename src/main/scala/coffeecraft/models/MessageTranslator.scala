package coffeecraft.models

import coffeecraft.dao.{ListAll, Post, Delete, Get}

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "POST" :: name :: price :: Nil =>
      Post(Coffee(name.trim, price.toFloat))
    case "GET" :: name :: Nil =>
      Get(name.toInt)
    case "DEL" :: name :: Nil =>
      Delete(name.toInt)
    case "LIST" :: Nil =>
      ListAll
  }
}

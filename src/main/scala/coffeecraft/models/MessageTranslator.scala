package coffeecraft.models

import coffeecraft.dao._

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "POST" :: name :: price :: Nil =>
      Insert(Coffee(name.trim, price.toFloat))
    case "PUT" :: id :: name :: price :: Nil =>
      Update(id.toInt, Coffee(name, price.toFloat))
    case "GET" :: id :: Nil =>
      Fetch(id.toInt)
    case "DEL" :: id :: Nil =>
      Remove(id.toInt)
    case "LIST" :: Nil =>
      FetchAll
    case _ =>
      CommandError
  }
}

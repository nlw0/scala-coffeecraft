package coffeecraft.models

import coffeecraft.dao._

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "POST" :: name :: price :: Nil =>
      Post(Coffee(name.trim, price.toFloat))
    case "PUT" :: id :: name :: price :: Nil =>
      Update(id.toInt, Coffee(name, price.toFloat))
    case "GET" :: id :: Nil =>
      Get(id.toInt)
    case "DEL" :: id :: Nil =>
      Delete(id.toInt)
    case "LIST" :: Nil =>
      ListAll
  }
}

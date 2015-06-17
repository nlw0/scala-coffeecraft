package coffeecraft.models

import coffeecraft.dao._

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "POST" :: name :: price :: Nil =>
      (ToCoffee, Insert(Coffee(name.trim, price.toFloat)))
    case "PUT" :: id :: name :: price :: Nil =>
      (ToCoffee, Update(id.toInt, Coffee(name, price.toFloat)))
    case "GET" :: id :: Nil =>
      (ToCoffee, Fetch(id.toInt))
    case "DEL" :: id :: Nil =>
      (ToCoffee, Remove(id.toInt))
    case "LIST" :: Nil =>
      (ToCoffee, FetchAll)

    case "INVADD" :: uid :: cid :: Nil =>
      (ToInventory, Insert(InventoryItem(uid.toInt, cid.toInt)))
    case "INVLIST" :: Nil =>
      (ToInventory, FetchAll)
    case "INVDEL" :: uid :: cid :: Nil =>
      (ToInventory, Insert(InventoryItem(uid.toInt, cid.toInt)))

    case _ =>
      (ToCoffee, CommandError)
  }
}

trait CmdDest

case object ToCoffee extends CmdDest

case object ToInventory extends CmdDest
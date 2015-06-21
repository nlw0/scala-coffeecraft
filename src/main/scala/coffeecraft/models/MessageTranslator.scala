package coffeecraft.models

import coffeecraft.camelserver._

object MessageTranslator {
  def apply(input: String) = input.split(" ").toList match {
    case "LIST" :: Nil =>
      ListCmd
    case "MINE" :: Nil =>
      MineCmd
    case "CRAFT" :: userIdS :: inventoryIds =>
      CraftCmd(inventoryIds map (_.toLong) toSet)
    case _ =>

  }
}

trait CmdDest

case object ToCoffee extends CmdDest

case object ToInventory extends CmdDest
